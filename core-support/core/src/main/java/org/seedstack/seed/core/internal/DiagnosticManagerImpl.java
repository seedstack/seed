/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal;

import com.google.common.collect.Maps;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import org.reflections.util.ClasspathHelper;
import org.seedstack.seed.core.api.DiagnosticManager;
import org.seedstack.seed.core.api.SeedException;
import org.seedstack.seed.core.api.CoreErrorCode;
import org.seedstack.seed.core.spi.diagnostic.DiagnosticInfoCollector;
import org.seedstack.seed.core.spi.diagnostic.DiagnosticReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of the diagnostic manager.
 *
 * @author adrien.lauer@mpsa.com
 */
class DiagnosticManagerImpl implements DiagnosticManager {
    public static final String REPORTER_SYSTEM_PROPERTY = "org.seedstack.seed.diagnostic.reporter";
    private static final Logger LOGGER = LoggerFactory.getLogger(DiagnosticManagerImpl.class);
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss.SSS");

    private final DiagnosticReporter diagnosticReporter;

    @Inject
    private Injector injector;
    private Set<URL> classpathUrls;

    DiagnosticManagerImpl() {
        String reporterClassName = System.getProperty(REPORTER_SYSTEM_PROPERTY);
        DiagnosticReporter diagnosticReporterToUse;

        if (reporterClassName != null) {
            try {
                diagnosticReporterToUse = (DiagnosticReporter) Class.forName(reporterClassName).newInstance();
            } catch (Exception e) {
                LOGGER.warn("Custom diagnostic reporter ({}) cannot be instantiated, fallback to default reporter", reporterClassName);
                LOGGER.debug(CorePlugin.DETAILS_MESSAGE, e);
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
            throw SeedException.wrap(t, CoreErrorCode.RETROW_EXCEPTION_AFTER_DIAGNOSTIC_FAILURE);
        }
    }

    void setClasspathUrls(Set<URL> classpathUrls) {
        this.classpathUrls = new HashSet<URL>(classpathUrls);
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

        result.put("classpath", getClasspath());
        if (classpathUrls == null) {
            result.put("classpath-info", "Classpath is based on class loader information and java.class.path system property");
        } else {
            result.put("classpath-info", "Classpath is based on the classpath scanned by SEED");
        }

        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        result.put("args", runtimeMXBean.getInputArguments());
        result.put("start-time", DATE_FORMAT.format(runtimeMXBean.getStartTime()));

        return result;
    }

    private Map<Long, Object> buildThreadList() {
        Map<Long, Object> results = new HashMap<Long, Object>();
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        for (long threadId : threadMXBean.getAllThreadIds()) {
            Map<String, Object> threadResults = new HashMap<String, Object>();
            ThreadInfo threadInfo = threadMXBean.getThreadInfo(threadId);
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

        return results;
    }

    private List<String> getClasspath() {
        List<String> urls = new ArrayList<String>();

        if (classpathUrls != null) {
            for (URL classpathUrl : classpathUrls) {
                urls.add(classpathUrl.toExternalForm());
            }
        } else {
            Set<URL> coreClasspathUrls = new HashSet<URL>();

            coreClasspathUrls.addAll(ClasspathHelper.forJavaClassPath());
            coreClasspathUrls.addAll(ClasspathHelper.forClassLoader());

            for (URL coreClasspathUrl : coreClasspathUrls) {
                urls.add(coreClasspathUrl.toExternalForm());
            }
        }

        return urls;
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
