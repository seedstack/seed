/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.diagnostic;

import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.Context;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.api.plugin.request.ClasspathScanRequest;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import org.seedstack.seed.core.SeedRuntime;
import org.seedstack.seed.core.internal.AbstractSeedPlugin;
import org.seedstack.seed.diagnostic.DiagnosticManager;
import org.seedstack.seed.diagnostic.spi.DiagnosticDomain;
import org.seedstack.seed.diagnostic.spi.DiagnosticInfoCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Core plugin that detects diagnostic collectors and registers them with the diagnostic manager.
 */
public class DiagnosticPlugin extends AbstractSeedPlugin {
    private static final Logger LOGGER = LoggerFactory.getLogger(DiagnosticPlugin.class);
    private final Map<String, Class<? extends DiagnosticInfoCollector>> diagnosticInfoCollectorClasses = new
            HashMap<>();
    private DiagnosticManager diagnosticManager;
    @Inject
    private Map<String, DiagnosticInfoCollector> diagnosticInfoCollectors;

    @Override
    public String name() {
        return "diagnostic";
    }

    @Override
    protected void setup(SeedRuntime seedRuntime) {
        diagnosticManager = seedRuntime.getDiagnosticManager();
    }

    @Override
    public Collection<ClasspathScanRequest> classpathScanRequests() {
        return classpathScanRequestBuilder()
                .subtypeOf(DiagnosticInfoCollector.class)
                .build();
    }

    @SuppressWarnings("unchecked")
    @Override
    public InitState initialize(InitContext initContext) {
        initContext.scannedSubTypesByParentClass().get(DiagnosticInfoCollector.class)
                .stream()
                .filter(DiagnosticInfoCollector.class::isAssignableFrom)
                .forEach(candidate -> {
                    DiagnosticDomain diagnosticDomain = candidate.getAnnotation(DiagnosticDomain.class);
                    if (diagnosticDomain != null) {
                        diagnosticInfoCollectorClasses.put(diagnosticDomain.value(),
                                (Class<? extends DiagnosticInfoCollector>) candidate);
                        LOGGER.trace("Detected diagnostic collector {} for diagnostic domain {}",
                                candidate.getCanonicalName(), diagnosticDomain.value());
                    }
                });
        LOGGER.debug("Detected {} diagnostic collector(s)", diagnosticInfoCollectorClasses.size());

        return InitState.INITIALIZED;
    }

    @Override
    public Object nativeUnitModule() {
        return new DiagnosticModule(diagnosticManager, diagnosticInfoCollectorClasses);
    }

    @Override
    public void start(Context context) {
        for (Map.Entry<String, DiagnosticInfoCollector> entry : diagnosticInfoCollectors.entrySet()) {
            diagnosticManager.registerDiagnosticInfoCollector(entry.getKey(), entry.getValue());
        }
    }
}
