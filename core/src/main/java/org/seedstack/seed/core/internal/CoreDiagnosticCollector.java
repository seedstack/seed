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
            return new HashSet<URL>((Set<URL>) SeedReflectionUtils.invokeMethod(
                    SeedReflectionUtils.getFieldValue(
                            SeedReflectionUtils.getFieldValue(initContext,
                                    "requestHandler"),
                            "classpathScanner"),
                    "computeUrls"));
        } catch (Exception e) {
            SeedLoggingUtils.logWarningWithDebugDetails(LOGGER, e, "Unable to collect scanned classpath");
        }

        return null;
    }
}
