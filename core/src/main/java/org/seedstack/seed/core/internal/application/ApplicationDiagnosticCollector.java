/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.application;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationConverter;
import org.seedstack.seed.spi.diagnostic.DiagnosticInfoCollector;

import java.util.HashMap;
import java.util.Map;

/**
 * This diagnostic collector provides information about the application itself (configuration).
 *
 * @author adrien.lauer@mpsa.com
 */
class ApplicationDiagnosticCollector implements DiagnosticInfoCollector {
    private String basePackages;
    private Configuration configuration;
    private String activeProfiles;
    private String storageLocation;
    private ApplicationInfo applicationInfo;

    @Override
    public Map<String, Object> collect() {
        Map<String, Object> result = new HashMap<String, Object>();

        if (applicationInfo != null) {
            result.put("id", applicationInfo.getId());
            result.put("name", applicationInfo.getName());
            result.put("version", applicationInfo.getVersion());
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

        if (configuration != null) {
            result.put("base-packages", basePackages);
        }

        return result;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public void setActiveProfiles(String activeProfiles) {
        this.activeProfiles = activeProfiles;
    }

    public void setStorageLocation(String storageLocation) {
        this.storageLocation = storageLocation;
    }

    public void setBasePackages(String basePackages) {
        this.basePackages = basePackages;
    }

    public void setApplicationInfo(ApplicationInfo applicationInfo) {
        this.applicationInfo = applicationInfo;
    }
}
