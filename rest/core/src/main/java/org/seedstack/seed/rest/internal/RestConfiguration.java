/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.internal;

import org.apache.commons.configuration.Configuration;
import org.seedstack.seed.core.utils.SeedConfigurationUtils;

import java.util.Properties;

public class RestConfiguration {
    private static final String PREFIX = "org.seedstack.seed.rest";

    private String restPath;
    private String jspPath;
    private String baseRel;
    private String baseParam;
    private boolean jsonHomeEnabled;
    private boolean securityExceptionMappingEnabled;
    private boolean exceptionMappingEnabled;
    private Properties jerseyProperties;

    public String getRestPath() {
        return restPath;
    }

    public String getJspPath() {
        return jspPath;
    }

    public String getBaseRel() {
        return baseRel;
    }

    public String getBaseParam() {
        return baseParam;
    }

    public boolean isJsonHomeEnabled() {
        return jsonHomeEnabled;
    }

    public boolean isSecurityExceptionMappingEnabled() {
        return securityExceptionMappingEnabled;
    }

    public boolean isExceptionMappingEnabled() {
        return exceptionMappingEnabled;
    }

    public Properties getJerseyProperties() {
        return jerseyProperties;
    }

    void init(Configuration configuration) {
        Configuration restConfiguration = configuration.subset(PREFIX);

        restPath = restConfiguration.getString("path", "");
        jspPath = restConfiguration.getString("jsp-path", "/WEB-INF/jsp");
        baseRel = restConfiguration.getString("baseRel", "");
        baseParam = restConfiguration.getString("baseParam", "");
        jsonHomeEnabled = !(restConfiguration.getBoolean("disable-json-home", false));
        securityExceptionMappingEnabled = restConfiguration.getBoolean("map-security-exceptions", true);
        exceptionMappingEnabled = restConfiguration.getBoolean("map-all-exceptions", true);
        jerseyProperties = SeedConfigurationUtils.buildPropertiesFromConfiguration(restConfiguration, "jersey.property");
    }
}
