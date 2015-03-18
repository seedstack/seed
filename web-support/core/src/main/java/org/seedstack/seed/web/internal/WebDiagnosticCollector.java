/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web.internal;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import org.seedstack.seed.core.spi.diagnostic.DiagnosticDomain;
import org.seedstack.seed.core.spi.diagnostic.DiagnosticInfoCollector;

import javax.servlet.ServletContext;
import java.util.HashMap;
import java.util.Map;

import static org.seedstack.seed.core.utils.SeedReflectionUtils.invokeMethod;

@DiagnosticDomain(WebPlugin.WEB_PLUGIN_PREFIX)
class WebDiagnosticCollector implements DiagnosticInfoCollector {
    @Inject(optional = true)
    private ServletContext servletContext;

    @Override
    public Map<String, Object> collect() {
        Map<String, Object> result = new HashMap<String, Object>();

        if (servletContext != null) {
            result.put("version", String.format("%d.%d", servletContext.getMajorVersion(), servletContext.getMinorVersion()));
            result.put("effective-version", String.format("%d.%d", invokeMethod(servletContext, "getEffectiveMajorVersion"), invokeMethod(servletContext, "getEffectiveMinorVersion")));
            result.put("servlets", buildServletList());
            result.put("filters", buildFilterList());
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> buildServletList() {
        Map<String, Object> servletMap = new HashMap<String, Object>();
        if (servletContext.getMajorVersion() >= 3) {
            for (Map.Entry<String, ?> servletRegistrationEntry : ((Map<String, ?>) invokeMethod(servletContext, "getServletRegistrations")).entrySet()) {
                Object servletRegistration = servletRegistrationEntry.getValue();
                Map<String, Object> servletRegistrationInfo = new HashMap<String, Object>();

                servletRegistrationInfo.put("class", invokeMethod(servletRegistration, "getClassName"));
                servletRegistrationInfo.put("parameters", invokeMethod(servletRegistration, "getInitParameters"));
                servletRegistrationInfo.put("mappings", Lists.newArrayList(invokeMethod(servletRegistration, "getMappings")));

                servletMap.put(servletRegistrationEntry.getKey(), servletRegistrationInfo);
            }
        }

        return servletMap;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> buildFilterList() {
        Map<String, Object> filterMap = new HashMap<String, Object>();
        if (servletContext.getMajorVersion() >= 3) {
            for (Map.Entry<String, ?> filterRegistrationEntry : ((Map<String, ?>) invokeMethod(servletContext, "getFilterRegistrations")).entrySet()) {
                Object filterRegistration = filterRegistrationEntry.getValue();
                Map<String, Object> filterRegistrationInfo = new HashMap<String, Object>();

                filterRegistrationInfo.put("class", invokeMethod(filterRegistration, "getClassName"));
                filterRegistrationInfo.put("parameters", invokeMethod(filterRegistration, "getInitParameters"));
                filterRegistrationInfo.put("servlet-name-mappings", Lists.newArrayList(invokeMethod(filterRegistration, "getServletNameMappings")));
                filterRegistrationInfo.put("url-pattern-mappings", Lists.newArrayList(invokeMethod(filterRegistration, "getUrlPatternMappings")));

                filterMap.put(filterRegistrationEntry.getKey(), filterRegistrationInfo);
            }
        }

        return filterMap;
    }
}
