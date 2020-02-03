/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web.internal.diagnostic;

import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;
import org.seedstack.seed.diagnostic.spi.DiagnosticInfoCollector;

class WebDiagnosticCollector implements DiagnosticInfoCollector {
    private final ServletContext servletContext;

    WebDiagnosticCollector(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Override
    public Map<String, Object> collect() {
        Map<String, Object> result = new HashMap<>();

        if (servletContext != null) {
            result.put("version",
                    String.format("%d.%d", servletContext.getMajorVersion(), servletContext.getMinorVersion()));
            if (servletContext.getMajorVersion() >= 3) {
                try {
                    result.put("effectiveVersion", String.format("%d.%d", servletContext.getEffectiveMajorVersion(),
                            servletContext.getEffectiveMinorVersion()));
                    result.put("servlets", buildServletList());
                    result.put("filters", buildFilterList());
                } catch (UnsupportedOperationException e) {
                    // nothing to do
                }
            }
        }
        return result;
    }

    private Map<String, Object> buildServletList() {
        Map<String, Object> servletMap = new HashMap<>();
        for (Map.Entry<String, ? extends ServletRegistration> servletRegistrationEntry : servletContext
                .getServletRegistrations().entrySet()) {
            ServletRegistration servletRegistration = servletRegistrationEntry.getValue();
            Map<String, Object> servletRegistrationInfo = new HashMap<>();

            servletRegistrationInfo.put("class", servletRegistration.getClassName());
            servletRegistrationInfo.put("parameters", servletRegistration.getInitParameters());
            Collection<String> mappings = servletRegistration.getMappings();
            if (mappings == null) {
                mappings = new ArrayList<>();
            }
            servletRegistrationInfo.put("mappings", Sets.newLinkedHashSet(mappings));

            servletMap.put(servletRegistrationEntry.getKey(), servletRegistrationInfo);
        }

        return servletMap;
    }

    private Map<String, Object> buildFilterList() {
        Map<String, Object> filterMap = new HashMap<>();
        for (Map.Entry<String, ? extends FilterRegistration> filterRegistrationEntry : servletContext
                .getFilterRegistrations().entrySet()) {
            FilterRegistration filterRegistration = filterRegistrationEntry.getValue();
            Map<String, Object> filterRegistrationInfo = new HashMap<>();

            filterRegistrationInfo.put("class", filterRegistration.getClassName());
            filterRegistrationInfo.put("parameters", filterRegistration.getInitParameters());
            Collection<String> mappings = filterRegistration.getServletNameMappings();
            if (mappings == null) {
                mappings = new ArrayList<>();
            }
            filterRegistrationInfo.put("servletNameMappings", Sets.newLinkedHashSet(mappings));
            Collection<String> patterns = filterRegistration.getUrlPatternMappings();
            if (patterns == null) {
                patterns = new ArrayList<>();
            }
            filterRegistrationInfo.put("urlPatternMappings", Sets.newLinkedHashSet(patterns));

            filterMap.put(filterRegistrationEntry.getKey(), filterRegistrationInfo);
        }

        return filterMap;
    }

}
