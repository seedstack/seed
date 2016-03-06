/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.init;

import com.google.common.collect.Maps;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import org.seedstack.seed.DiagnosticManager;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.core.internal.CoreErrorCode;
import org.seedstack.seed.core.utils.SeedLoggingUtils;
import org.seedstack.seed.spi.diagnostic.DiagnosticInfoCollector;
import org.seedstack.seed.spi.diagnostic.DiagnosticReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Implementation of the diagnostic manager.
 *
 * @author adrien.lauer@mpsa.com
 */
public class DiagnosticManagerImpl implements DiagnosticManager {
    public static final String REPORTER_SYSTEM_PROPERTY = "org.seedstack.seed.diagnostic.reporter";
    private static final Logger LOGGER = LoggerFactory.getLogger(DiagnosticManagerImpl.class);
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss.SSS");

    private final DiagnosticReporter diagnosticReporter;

    @Inject
    private Injector injector;

    public DiagnosticManagerImpl() {
        String reporterClassName = System.getProperty(REPORTER_SYSTEM_PROPERTY);
        DiagnosticReporter diagnosticReporterToUse;

        if (reporterClassName != null) {
            try {
                diagnosticReporterToUse = (DiagnosticReporter) Class.forName(reporterClassName).newInstance();
            } catch (Exception e) {
                SeedLoggingUtils.logWarningWithDebugDetails(LOGGER, e, "Custom diagnostic reporter ({}) cannot be instantiated, fallback to default reporter", reporterClassName);
                diagnosticReporterToUse = new DefaultDiagnosticReporter();
            }
        } else {
            diagnosticReporterToUse = new DefaultDiagnosticReporter();
        }

        this.diagnosticReporter = diagnosticReporterToUse;
    }

    @Override
    public Map<String, Object> getDiagnosticInfo(Throwable t) {
        return collectAllDiagnostics(t);
    }

    @Override
    public void dumpDiagnosticReport(Throwable t) {
        try {
            diagnosticReporter.writeDiagnosticReport(collectAllDiagnostics(t));
        } catch (Exception e) {
            LOGGER.error("Unable to write diagnostic information", e);
            throw SeedException.wrap(t, CoreErrorCode.RETHROW_EXCEPTION_AFTER_DIAGNOSTIC_FAILURE);
        }
    }

    private synchronized Map<String, Object> collectAllDiagnostics(Throwable t) {
        Map<String, Object> allDiagnostics = new HashMap<String, Object>();

        if (t != null) {
            Map<String, Object> exceptionInfo = new HashMap<String, Object>();
            buildExceptionInfo(exceptionInfo, t);
            allDiagnostics.put("exception", exceptionInfo);
        }

        allDiagnostics.put("system", collectSystemInfo());

        if (injector != null) {
            Map<String, DiagnosticInfoCollector> detectedDiagnosticInfoCollectors = injector.getInstance(Key.get(new TypeLiteral<Map<String, DiagnosticInfoCollector>>() {
            }));

            for (Map.Entry<String, DiagnosticInfoCollector> diagnosticInfoCollectorEntry : detectedDiagnosticInfoCollectors.entrySet()) {
                allDiagnostics.put(diagnosticInfoCollectorEntry.getKey(), diagnosticInfoCollectorEntry.getValue().collect());
            }
        }

        return allDiagnostics;
    }

    private void buildExceptionInfo(Map<String, Object> exceptionInfo, Throwable t) {
        exceptionInfo.put("class", t.getClass().getCanonicalName());
        exceptionInfo.put("message", t.getMessage());

        List<String> stackTraceList = new ArrayList<String>();
        for (StackTraceElement stackTraceElement : t.getStackTrace()) {
            stackTraceList.add(stackTraceElement.toString());
        }
        exceptionInfo.put("stacktrace", stackTraceList);

        if (t instanceof SeedException) {
            SeedException seedException = (SeedException) t;
            exceptionInfo.put("causes", seedException.getCauses());
            exceptionInfo.put("fix", seedException.getFix());
        } else {
            Throwable cause = t.getCause();
            if (cause != null) {
                Map<String, Object> causeInfo = new HashMap<String, Object>();
                buildExceptionInfo(causeInfo, cause);

                // only recurse when it is not a SEED exception
                exceptionInfo.put("cause", causeInfo);
            }
        }
    }

    private Map<String, Object> collectSystemInfo() {
        Map<String, Object> result = new HashMap<String, Object>();

        result.put("diagnostic-time", DATE_FORMAT.format(new Date()));
        result.put("properties", buildSystemPropertiesList());
        result.put("threads", buildThreadList());

        Runtime runtime = Runtime.getRuntime();
        result.put("processors", runtime.availableProcessors());
        result.put("memory", buildMemoryInfo(runtime));

        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        result.put("args", runtimeMXBean.getInputArguments());
        result.put("start-time", DATE_FORMAT.format(runtimeMXBean.getStartTime()));

        return result;
    }

    private Map<Long, Object> buildThreadList() {
        Map<Long, Object> results = new HashMap<Long, Object>();
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        for (long threadId : threadMXBean.getAllThreadIds()) {
            ThreadInfo threadInfo = threadMXBean.getThreadInfo(threadId);
            
            if (threadInfo != null) { // checks if the thread is not alive or it does not exist.
                Map<String, Object> threadResults = new HashMap<String, Object>();

                threadResults.put("name", threadInfo.getThreadName());
                threadResults.put("cpu", threadMXBean.getThreadCpuTime(threadId));
                threadResults.put("user", threadMXBean.getThreadUserTime(threadId));
                threadResults.put("blocked-count", threadInfo.getBlockedCount());
                threadResults.put("blocked-time", threadInfo.getBlockedTime());
                threadResults.put("waited-count", threadInfo.getWaitedCount());
                threadResults.put("waited-time", threadInfo.getWaitedTime());
                threadResults.put("suspended", threadInfo.isSuspended());
                threadResults.put("native", threadInfo.isInNative());
                threadResults.put("state", threadInfo.getThreadState().toString());

                results.put(threadId, threadResults);
            }
        }

        return results;
    }

    private Map<String, String> buildSystemPropertiesList() {
        return Maps.fromProperties(System.getProperties());
    }

    private Map<String, Long> buildMemoryInfo(Runtime runtime) {
        Map<String, Long> results = new HashMap<String, Long>();

        results.put("total", runtime.totalMemory());
        results.put("used", runtime.totalMemory() - runtime.freeMemory());
        results.put("free", runtime.freeMemory());
        results.put("max", runtime.maxMemory());

        return results;
    }
}
