/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web.internal;

import com.google.common.collect.Lists;
import com.google.inject.Module;
import com.google.inject.servlet.ServletModule;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.api.plugin.request.ClasspathScanRequest;
import io.nuun.kernel.core.AbstractPlugin;
import org.apache.commons.configuration.Configuration;
import org.reflections.util.ClasspathHelper;
import org.seedstack.seed.core.internal.CorePlugin;
import org.seedstack.seed.core.spi.configuration.ConfigurationProvider;
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
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This plugin provides web support for an application, more specifically:
 * <ul>
 * <li>detection and installation of servlets</li>
 * <li>detection and installation of filters</li>
 * <li>detection and installation of context listener</li>
 * <li>serving of static resources embedded in jars</li>
 * <li>optional per-request diagnostic dump</li>
 * </ul>
 *
 * @author adrien.lauer@mpsa.com
 */
public class WebPlugin extends AbstractPlugin {
    public static final String WEB_PLUGIN_PREFIX = "org.seedstack.seed.web";
    private static final Logger LOGGER = LoggerFactory.getLogger(WebPlugin.class);

    private final Set<ServletModule> additionalModules = new HashSet<ServletModule>();

    private ServletContext servletContext;
    private WebModule webModule;

    @Override
    public String name() {
        return "seed-web-plugin";
    }

    @SuppressWarnings("unchecked")
    @Override
    public InitState init(InitContext initContext) {
        if (servletContext == null) {
            LOGGER.info("No servlet context detected, web support disabled");
            return InitState.INITIALIZED;
        }

        WebDiagnosticCollector webDiagnosticCollector = new WebDiagnosticCollector(servletContext);

        initContext.dependency(CorePlugin.class).registerDiagnosticCollector(WEB_PLUGIN_PREFIX, webDiagnosticCollector);

        Configuration webConfiguration = initContext.dependency(ConfigurationProvider.class).getConfiguration().subset(WebPlugin.WEB_PLUGIN_PREFIX);
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

        boolean resourcesEnabled = webConfiguration.getBoolean("resources.enabled", true);
        if (resourcesEnabled) {
            LOGGER.info("Static resources served on /");
        }

        boolean requestDiagnosticEnabled = webConfiguration.getBoolean("request-diagnostic.enabled", false);
        if (requestDiagnosticEnabled) {
            LOGGER.info("Per-request diagnostic enabled");
        }

        webModule = new WebModule(
                requestDiagnosticEnabled,
                servlets,
                filters,
                resourcesEnabled,
                additionalModules
        );

        return InitState.INITIALIZED;
    }

    @Override
    public Set<URL> computeAdditionalClasspathScan() {
        Set<URL> additionalUrls = new HashSet<URL>();

        // resource paths for WEB-INF/lib can be null when SEED run in the Undertow servlet container
        if (servletContext != null && servletContext.getResourcePaths("/WEB-INF/lib") != null) {
            additionalUrls.addAll(ClasspathHelper.forWebInfLib(servletContext));
            additionalUrls.add(ClasspathHelper.forWebInfClasses(servletContext));
        }

        LOGGER.debug("{} URL(s) were determined from Web classpath", additionalUrls.size());

        return additionalUrls;
    }

    @Override
    public void provideContainerContext(Object containerContext) {
        if (containerContext != null && ServletContext.class.isAssignableFrom(containerContext.getClass())) {
            this.servletContext = (ServletContext) containerContext;
        }
    }

    @Override
    public Collection<ClasspathScanRequest> classpathScanRequests() {
        return classpathScanRequestBuilder().annotationType(WebServlet.class).annotationType(WebFilter.class).build();
    }

    @Override
    public Collection<Class<?>> requiredPlugins() {
        return Lists.<Class<?>>newArrayList(CorePlugin.class, ConfigurationProvider.class);
    }

    @Override
    public Module nativeUnitModule() {
        return webModule;
    }

    /**
     * Register additional an servlet module to be installed after this support one.
     *
     * @param servletModule the additional servlet module to register.
     */
    public void registerAdditionalModule(ServletModule servletModule) {
        additionalModules.add(servletModule);
    }
}
