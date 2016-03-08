/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal;

import io.nuun.kernel.api.plugin.context.InitContext;
import org.seedstack.seed.core.utils.SeedLoggingUtils;
import org.seedstack.seed.core.utils.SeedReflectionUtils;
import org.seedstack.seed.spi.diagnostic.DiagnosticInfoCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class CoreDiagnosticCollector implements DiagnosticInfoCollector {
    private static final Logger LOGGER = LoggerFactory.getLogger(CorePlugin.class);
    private final Set<URL> scannedUrls;

    CoreDiagnosticCollector(InitContext initContext) {
        scannedUrls = extractScannedUrls(initContext);
    }

    @Override
    public Map<String, Object> collect() {
        HashMap<String, Object> diagnosticInfo = new HashMap<String, Object>();

        if (scannedUrls != null) {
            diagnosticInfo.put("scanned-urls", scannedUrls);
        }

        return diagnosticInfo;
    }

    @SuppressWarnings("unchecked")
    private Set<URL> extractScannedUrls(InitContext initContext) {
        try {
            return new HashSet<URL>((Set<URL>) SeedReflectionUtils.invokeMethod(SeedReflectionUtils.getFieldValue(unproxify(initContext), "classpathScanner"), "computeUrls"));
        } catch (Exception e) {
            SeedLoggingUtils.logWarningWithDebugDetails(LOGGER, e, "Unable to collect scanned classpath");
        }

        return null;
    }

    // TODO remove this when not needed anymore (see at call site)
    private InitContext unproxify(InitContext initContext) throws Exception {
        InvocationHandler invocationHandler;
        try {
            invocationHandler = Proxy.getInvocationHandler(initContext);
        } catch (IllegalArgumentException e) {
            // not a proxy
            return initContext;
        }
        Field field = invocationHandler.getClass().getDeclaredField("val$initContext");
        field.setAccessible(true);
        return (InitContext) field.get(invocationHandler);
    }
}
