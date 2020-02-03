/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.diagnostic;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;
import java.util.Map;
import org.seedstack.seed.diagnostic.DiagnosticManager;
import org.seedstack.seed.diagnostic.spi.DiagnosticInfoCollector;

class DiagnosticModule extends AbstractModule {
    private final DiagnosticManager diagnosticManager;
    private final Map<String, Class<? extends DiagnosticInfoCollector>> diagnosticInfoCollectorClasses;

    DiagnosticModule(DiagnosticManager diagnosticManager,
            Map<String, Class<? extends DiagnosticInfoCollector>> diagnosticInfoCollectorClasses) {
        this.diagnosticManager = diagnosticManager;
        this.diagnosticInfoCollectorClasses = diagnosticInfoCollectorClasses;
    }

    @Override
    protected void configure() {
        MapBinder<String, DiagnosticInfoCollector> diagnosticInfoCollectorMapBinder = MapBinder.newMapBinder(binder(),
                String.class, DiagnosticInfoCollector.class);
        for (Map.Entry<String, Class<? extends DiagnosticInfoCollector>> diagnosticInfoCollectorEntry :
                diagnosticInfoCollectorClasses.entrySet()) {
            diagnosticInfoCollectorMapBinder.addBinding(diagnosticInfoCollectorEntry.getKey()).to(
                    diagnosticInfoCollectorEntry.getValue());
        }
        bind(DiagnosticManager.class).toInstance(diagnosticManager);
    }
}
