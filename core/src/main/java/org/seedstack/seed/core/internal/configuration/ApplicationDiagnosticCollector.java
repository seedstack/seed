/*
 * Copyright © 2013-2021, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.configuration;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.seedstack.seed.Application;
import org.seedstack.seed.diagnostic.spi.DiagnosticInfoCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

class ApplicationDiagnosticCollector implements DiagnosticInfoCollector {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationDiagnosticCollector.class);
    private final YAMLMapper yamlMapper = new YAMLMapper();
    private final Application application;

    ApplicationDiagnosticCollector(Application application) {
        this.application = application;
    }

    @Override
    public Map<String, Object> collect() {
        Map<String, Object> result = new HashMap<>();

        result.put("id", application.getId());
        result.put("name", application.getName());
        result.put("version", application.getVersion());
        if (application.isStorageEnabled()) {
            result.put("storage", application.getStorageLocation(""));
        }

        try {
            result.put("configuration", yamlMapper.readValue(application.getConfiguration().toString(), Map.class));
        } catch (IOException | RuntimeException e) {
            LOGGER.warn("Error building diagnostic configuration, using toString() instead", e);
            result.put("configuration", application.getConfiguration().toString());
        }

        result.put("configurationProfiles", application.getConfigurationProfiles());

        return result;
    }
}
