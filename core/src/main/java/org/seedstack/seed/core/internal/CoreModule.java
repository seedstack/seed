/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.PrivateModule;
import com.google.inject.matcher.Matchers;
import com.google.inject.multibindings.MapBinder;
import org.seedstack.seed.core.api.DiagnosticManager;
import org.seedstack.seed.core.spi.diagnostic.DiagnosticInfoCollector;
import org.seedstack.seed.core.utils.SeedCheckUtils;

import java.util.Collection;
import java.util.Map;

/**
 * Guice module for SEED core functionality.
 *
 * @author adrien.lauer@mpsa.com
 */
class CoreModule extends AbstractModule {
    private final Collection<Module> subModules;
    private final DiagnosticManager diagnosticManager;
    private final Map<String, DiagnosticInfoCollector> diagnosticInfoCollectors;

    CoreModule(Collection<Module> subModules, DiagnosticManager diagnosticManager, Map<String, DiagnosticInfoCollector> diagnosticInfoCollectors) {
        this.subModules = subModules;
        this.diagnosticManager = diagnosticManager;
        this.diagnosticInfoCollectors = diagnosticInfoCollectors;
    }

    @Override
    protected void configure() {
        install(new PrivateModule() {
            @Override
            protected void configure() {
                MapBinder<String, DiagnosticInfoCollector> diagnosticInfoCollectorMapBinder = MapBinder.newMapBinder(binder(), String.class, DiagnosticInfoCollector.class);
                for (Map.Entry<String, DiagnosticInfoCollector> diagnosticInfoCollectorEntry : diagnosticInfoCollectors.entrySet()) {
                    diagnosticInfoCollectorMapBinder.addBinding(diagnosticInfoCollectorEntry.getKey()).toInstance(diagnosticInfoCollectorEntry.getValue());
                }

                bind(DiagnosticManager.class).toInstance(diagnosticManager);
                expose(DiagnosticManager.class);
            }
        });

        requestStaticInjection(SeedCheckUtils.class);

        bindListener(Matchers.any(), new LoggingTypeListener());

        for (Module subModule : subModules) {
            install(subModule);
        }
    }
}
