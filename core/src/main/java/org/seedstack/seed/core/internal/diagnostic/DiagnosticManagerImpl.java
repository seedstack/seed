/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.diagnostic;

import com.google.common.collect.Maps;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.core.internal.CoreErrorCode;
import org.seedstack.seed.diagnostic.DiagnosticManager;
import org.seedstack.seed.diagnostic.spi.DiagnosticInfoCollector;
import org.seedstack.seed.diagnostic.spi.DiagnosticReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the diagnostic manager.
 */
public class DiagnosticManagerImpl implements DiagnosticManager {
    private static final String REPORTER_SYSTEM_PROPERTY = "seedstack.diagnostic";
    private static final Logger LOGGER = LoggerFactory.getLogger(DiagnosticManagerImpl.class);
    private static final String DATE_FORMAT = "yyyy-MM-dd-HH-mm-ss.SSS";

    private final ConcurrentMap<String, DiagnosticInfoCollector> diagnosticCollectors = new ConcurrentHashMap<>();
    private final DiagnosticReporter diagnosticReporter;

    public DiagnosticManagerImpl() {
        String reporterClassName = System.getProperty(REPORTER_SYSTEM_PROPERTY);
        DiagnosticReporter diagnosticReporterToUse;

        if (reporterClassName != null) {
            try {
                diagnosticReporterToUse = (DiagnosticReporter) Class.forName(reporterClassName).newInstance();
            } catch (Exception e) {
                LOGGER.warn("Custom diagnostic reporter {} cannot be instantiated, falling back to default reporter",
                        reporterClassName, e);
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

    @Override
    public void registerDiagnosticInfoCollector(String domain, DiagnosticInfoCollector diagnosticInfoCollector) {
        diagnosticCollectors.put(domain, diagnosticInfoCollector);
    }

    private synchronized Map<String, Object> collectAllDiagnostics(Throwable t) {
        Map<String, Object> allDiagnostics = new HashMap<>();

        if (t != null) {
            Map<String, Object> exceptionInfo = new HashMap<>();
            buildExceptionInfo(exceptionInfo, t);
            allDiagnostics.put("exception", exceptionInfo);
        }

        allDiagnostics.put("system", collectSystemInfo());

        for (Map.Entry<String, DiagnosticInfoCollector> diagnosticInfoCollectorEntry : diagnosticCollectors.entrySet
                ()) {
            allDiagnostics.put(diagnosticInfoCollectorEntry.getKey(),
                    diagnosticInfoCollectorEntry.getValue().collect());
        }

        return allDiagnostics;
    }

    private void buildExceptionInfo(Map<String, Object> exceptionInfo, Throwable t) {
        exceptionInfo.put("class", t.getClass().getCanonicalName());
        exceptionInfo.put("message", t.getMessage());

        List<String> stackTraceList = new ArrayList<>();
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
                Map<String, Object> causeInfo = new HashMap<>();
                buildExceptionInfo(causeInfo, cause);

                // only recurse when it is not a SeedStack exception
                exceptionInfo.put("cause", causeInfo);
            }
        }
    }

    private Map<String, Object> collectSystemInfo() {
        Map<String, Object> result = new HashMap<>();

        result.put("diagnosticTime", new SimpleDateFormat(DiagnosticManagerImpl.DATE_FORMAT).format(new Date()));
        result.put("properties", buildSystemPropertiesList());
        result.put("threads", buildThreadList());

        Runtime runtime = Runtime.getRuntime();
        result.put("processors", runtime.availableProcessors());
        result.put("memory", buildMemoryInfo(runtime));

        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        result.put("args", runtimeMXBean.getInputArguments());
        result.put("startTime",
                new SimpleDateFormat(DiagnosticManagerImpl.DATE_FORMAT).format(runtimeMXBean.getStartTime()));

        return result;
    }

    private Map<Long, Object> buildThreadList() {
        Map<Long, Object> results = new HashMap<>();
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        for (long threadId : threadMXBean.getAllThreadIds()) {
            ThreadInfo threadInfo = threadMXBean.getThreadInfo(threadId);

            if (threadInfo != null) { // checks if the thread is not alive or it does not exist.
                Map<String, Object> threadResults = new HashMap<>();

                threadResults.put("name", threadInfo.getThreadName());
                threadResults.put("cpu", threadMXBean.getThreadCpuTime(threadId));
                threadResults.put("user", threadMXBean.getThreadUserTime(threadId));
                threadResults.put("blockedCount", threadInfo.getBlockedCount());
                threadResults.put("blockedTime", threadInfo.getBlockedTime());
                threadResults.put("waitedCount", threadInfo.getWaitedCount());
                threadResults.put("waitedTime", threadInfo.getWaitedTime());
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
        Map<String, Long> results = new HashMap<>();

        results.put("total", runtime.totalMemory());
        results.put("used", runtime.totalMemory() - runtime.freeMemory());
        results.put("free", runtime.freeMemory());
        results.put("max", runtime.maxMemory());

        return results;
    }
}
