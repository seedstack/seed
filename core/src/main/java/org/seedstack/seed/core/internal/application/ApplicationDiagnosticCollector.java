/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.application;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationConverter;
import org.seedstack.seed.core.spi.diagnostic.DiagnosticInfoCollector;

import java.util.HashMap;
import java.util.Map;

/**
 * This diagnostic collector provides information about the application itself (configuration).
 *
 * @author adrien.lauer@mpsa.com
 */
class ApplicationDiagnosticCollector implements DiagnosticInfoCollector {
    private Configuration configuration;
    private String applicationId;
    private String applicationName;
    private String applicationVersion;
    private String activeProfiles;
    private String storageLocation;

    @Override
    public Map<String, Object> collect() {
        Map<String, Object> result = new HashMap<String, Object>();

        if (applicationId != null) {
            result.put("id", applicationId);
        }

        if (applicationName != null) {
            result.put("name", applicationName);
        }

        if (applicationVersion != null) {
            result.put("version", applicationVersion);
        }

        if (activeProfiles != null) {
            result.put("active-profiles", activeProfiles);
        }

        if (storageLocation != null) {
            result.put("storage-location", storageLocation);
        }

        if (configuration != null) {
            result.put("configuration", ConfigurationConverter.getMap(configuration));
        }

        return result;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public void setApplicationVersion(String applicationVersion) {
        this.applicationVersion = applicationVersion;
    }

    public void setActiveProfiles(String activeProfiles) {
        this.activeProfiles = activeProfiles;
    }

    public void setStorageLocation(String storageLocation) {
        this.storageLocation = storageLocation;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }
}
