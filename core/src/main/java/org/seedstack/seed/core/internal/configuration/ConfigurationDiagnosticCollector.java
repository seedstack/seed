/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.configuration;

import org.seedstack.seed.Application;
import org.seedstack.seed.spi.diagnostic.DiagnosticInfoCollector;

import java.util.HashMap;
import java.util.Map;

class ConfigurationDiagnosticCollector implements DiagnosticInfoCollector {
    private final Application application;

    ConfigurationDiagnosticCollector(Application application) {
        this.application = application;
    }

    @Override
    public Map<String, Object> collect() {
        Map<String, Object> result = new HashMap<>();

        Map<String, Object> applicationInfo = new HashMap<>();
        applicationInfo.put("id", application.getId());
        applicationInfo.put("name", application.getName());
        applicationInfo.put("version", application.getVersion());
        if (application.isStorageEnabled()) {
            applicationInfo.put("storage", application.getStorageLocation(""));
        }
        result.put("application", applicationInfo);

        return result;
    }
}
