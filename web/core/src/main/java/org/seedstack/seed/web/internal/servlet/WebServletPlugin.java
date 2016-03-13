/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web.internal.servlet;

import com.google.inject.Module;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.api.plugin.request.ClasspathScanRequest;
import io.nuun.kernel.core.AbstractPlugin;
import org.seedstack.seed.SeedRuntime;
import org.seedstack.seed.web.WebFilter;
import org.seedstack.seed.web.WebInitParam;
import org.seedstack.seed.web.WebServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import java.lang.annotation.Annotation;
import java.util.*;

/**
 * This plugin provides support for programmatically registering Servlet and Filters.
 *
 * @author adrien.lauer@mpsa.com
 */
public class WebServletPlugin extends AbstractPlugin {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebServletPlugin.class);

    private ServletContext servletContext;
    private WebServletModule webServletModule;

    @Override
    public String name() {
        return "web-servlet";
    }

    @Override
    public void provideContainerContext(Object containerContext) {
        servletContext = ((SeedRuntime) containerContext).contextAs(ServletContext.class);
    }

    @Override
    public Collection<ClasspathScanRequest> classpathScanRequests() {
        return classpathScanRequestBuilder().annotationType(WebServlet.class).annotationType(WebFilter.class).build();
    }

    @SuppressWarnings("unchecked")
    @Override
    public InitState init(InitContext initContext) {
        if (servletContext == null) {
            LOGGER.info("No servlet context detected, servlet and filters disabled");
            return InitState.INITIALIZED;
        }

        Map<Class<? extends Annotation>, Collection<Class<?>>> scannedClassesByAnnotationClass = initContext.scannedClassesByAnnotationClass();

        Collection<Class<?>> list = scannedClassesByAnnotationClass.get(WebServlet.class);
        List<ConfiguredServlet> servlets = new ArrayList<ConfiguredServlet>();
        for (Class<?> candidate : list) {
            if (Servlet.class.isAssignableFrom(candidate)) {
                WebServlet annotation = candidate.getAnnotation(WebServlet.class);

                ConfiguredServlet configuredServlet = new ConfiguredServlet();
                configuredServlet.setName(annotation.name());

                Map<String, String> initParams = new HashMap<String, String>();
                for (WebInitParam initParam : annotation.initParams()) {
                    initParams.put(initParam.name(), initParam.value());
                }
                configuredServlet.setInitParams(initParams);

                configuredServlet.setUrlPatterns(annotation.value());

                configuredServlet.setClazz((Class<? extends HttpServlet>) candidate);

                servlets.add(configuredServlet);
                LOGGER.debug("Serving {} with {}", Arrays.toString(configuredServlet.getUrlPatterns()), configuredServlet.getClazz().getCanonicalName());
            }
        }

        List<ConfiguredFilter> filters = new ArrayList<ConfiguredFilter>();
        for (Class<?> candidate : scannedClassesByAnnotationClass.get(WebFilter.class)) {
            if (Filter.class.isAssignableFrom(candidate)) {
                WebFilter annotation = candidate.getAnnotation(WebFilter.class);

                ConfiguredFilter configuredFilter = new ConfiguredFilter();
                configuredFilter.setName(annotation.filterName());

                Map<String, String> initParams = new HashMap<String, String>();
                for (WebInitParam initParam : annotation.initParams()) {
                    initParams.put(initParam.name(), initParam.value());
                }
                configuredFilter.setInitParams(initParams);

                configuredFilter.setUrlPatterns(annotation.value());

                configuredFilter.setClazz((Class<? extends Filter>) candidate);

                filters.add(configuredFilter);
                LOGGER.debug("Filtering {} through {}", Arrays.toString(configuredFilter.getUrlPatterns()), configuredFilter.getClazz().getCanonicalName());
            }
        }

        webServletModule = new WebServletModule(servlets, filters);

        return InitState.INITIALIZED;
    }

    @Override
    public Module nativeUnitModule() {
        return webServletModule;
    }
}
