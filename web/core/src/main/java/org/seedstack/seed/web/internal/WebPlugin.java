/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web.internal;

import static org.seedstack.shed.misc.PriorityUtils.sortByPriority;

import com.google.common.collect.Lists;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceFilter;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.Context;
import io.nuun.kernel.api.plugin.context.InitContext;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.servlet.ServletContext;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.core.SeedRuntime;
import org.seedstack.seed.core.internal.AbstractSeedPlugin;
import org.seedstack.seed.spi.ConfigurationPriority;
import org.seedstack.seed.web.spi.FilterDefinition;
import org.seedstack.seed.web.spi.ListenerDefinition;
import org.seedstack.seed.web.spi.SeedFilterPriority;
import org.seedstack.seed.web.spi.ServletDefinition;
import org.seedstack.seed.web.spi.WebProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This plugin provides support for servlet-based Web applications.
 */
public class WebPlugin extends AbstractSeedPlugin {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebPlugin.class);
    private static final String WEB_INF_LIB = "/WEB-INF/lib";
    private static final String WEB_INF_CLASSES = "/WEB-INF/classes";

    private final List<FilterDefinition> filterDefinitions = new ArrayList<>();
    private final List<ServletDefinition> servletDefinitions = new ArrayList<>();
    private final List<ListenerDefinition> listenerDefinitions = new ArrayList<>();
    private ServletContext servletContext;

    @Override
    public String name() {
        return "web";
    }

    @Override
    protected void setup(SeedRuntime seedRuntime) {
        servletContext = seedRuntime.contextAs(ServletContext.class);
        if (servletContext != null) {
            seedRuntime.registerConfigurationProvider(
                    new WebRuntimeConfigurationProvider(servletContext),
                    ConfigurationPriority.RUNTIME_INFO
            );
        }
    }

    @Override
    @SuppressFBWarnings(value = "DMI_COLLECTION_OF_URLS", justification = "Those URLs are denoting local resources "
            + "without DNS resolution")
    public Set<URL> computeAdditionalClasspathScan() {
        Set<URL> additionalUrls = new HashSet<>();
        if (servletContext != null) {
            Set<URL> webLibraries = resolveWebLibraries();
            if (webLibraries.isEmpty()) {
                LOGGER.debug("No Web library found for scanning");
            } else {
                for (URL webLibrary : webLibraries) {
                    LOGGER.trace("Resolved Web library {}", webLibrary);
                }
                LOGGER.debug("Found {} Web libraries for scanning", webLibraries.size());
            }
            additionalUrls.addAll(webLibraries);

            Set<URL> webClassesLocations = resolveWebClassesLocations();
            if (webClassesLocations.isEmpty()) {
                LOGGER.debug("No {} location found for scanning", WEB_INF_CLASSES);
            } else {
                for (URL webClassesLocation : webClassesLocations) {
                    LOGGER.trace("Resolved '{}' location: {}", WEB_INF_CLASSES, webClassesLocation);
                }
                LOGGER.debug("Found {} '{}' locations for scanning", webClassesLocations.size(), WEB_INF_CLASSES);
            }
            additionalUrls.addAll(webClassesLocations);
        }
        return additionalUrls;
    }

    @Override
    public Collection<Class<?>> dependencies() {
        return Lists.newArrayList(WebProvider.class);
    }

    @Override
    public InitState initialize(InitContext initContext) {
        if (servletContext != null) {
            List<WebProvider> webProviders = initContext.dependencies(WebProvider.class);

            // Always add Guice filter
            filterDefinitions.add(buildGuiceFilterDefinition());

            for (WebProvider webProvider : webProviders) {
                List<FilterDefinition> filters = webProvider.filters();
                if (filters != null) {
                    filterDefinitions.addAll(filters);
                }

                List<ServletDefinition> servlets = webProvider.servlets();
                if (servlets != null) {
                    servletDefinitions.addAll(servlets);
                }

                List<ListenerDefinition> listeners = webProvider.listeners();
                if (listeners != null) {
                    listenerDefinitions.addAll(listeners);
                }
            }

            // Sort filter according to the priority in their definition
            sortByPriority(filterDefinitions, FilterDefinition::getPriority);

            // Sort listeners according to the priority in their definition
            sortByPriority(listenerDefinitions, ListenerDefinition::getPriority);
        }

        return InitState.INITIALIZED;
    }

    @Override
    public Object nativeUnitModule() {
        return new WebModule();
    }

    @Override
    public void start(Context context) {
        if (servletContext != null) {
            ServletContextConfigurer servletContextConfigurer = new ServletContextConfigurer(servletContext,
                    context.applicationObjectGraph().as(Injector.class));

            for (ListenerDefinition listenerDefinition : listenerDefinitions) {
                LOGGER.debug("Registering servlet listener {} (priority {})",
                        listenerDefinition.getListenerClass().getName(),
                        listenerDefinition.getPriority());
                servletContextConfigurer.addListener(listenerDefinition);
            }
            LOGGER.debug("Registered {} servlet listener(s)", listenerDefinitions.size());

            for (FilterDefinition filterDefinition : filterDefinitions) {
                LOGGER.debug("Registering servlet filter {} (priority {})", filterDefinition.getFilterClass().getName(),
                        filterDefinition.getPriority());
                servletContextConfigurer.addFilter(filterDefinition);
            }
            LOGGER.debug("Registered {} servlet filter(s)", filterDefinitions.size());

            for (ServletDefinition servletDefinition : servletDefinitions) {
                LOGGER.debug("Registering servlet {}", servletDefinition.getServletClass().getName());
                servletContextConfigurer.addServlet(servletDefinition);
            }
            LOGGER.debug("Registered {} servlet(s)", servletDefinitions.size());
        }
    }

    private FilterDefinition buildGuiceFilterDefinition() {
        FilterDefinition guiceFilter = new FilterDefinition("guice", GuiceFilter.class);
        guiceFilter.setPriority(SeedFilterPriority.GUICE);
        guiceFilter.setAsyncSupported(true);
        guiceFilter.addMappings(new FilterDefinition.Mapping("/*"));
        return guiceFilter;
    }

    @SuppressFBWarnings(value = "DMI_COLLECTION_OF_URLS", justification = "Those URLs are denoting local resources "
            + "without DNS resolution")
    private Set<URL> resolveWebLibraries() {
        final Set<URL> resolvedUrls = new HashSet<>();
        Set<String> resourcePaths = servletContext.getResourcePaths(WEB_INF_LIB);
        if (resourcePaths != null) {
            for (String resourcePath : resourcePaths) {
                try {
                    URL resolvedURL = servletContext.getResource(resourcePath);
                    if (resolvedURL != null) {
                        resolvedUrls.add(resolvedURL);
                    } else {
                        LOGGER.warn("Ignoring unresolvable Web library {}", resourcePath);
                    }
                } catch (Exception e) {
                    throw SeedException.wrap(e, WebErrorCode.CANNOT_RESOLVE_WEB_RESOURCE_LOCATION)
                            .put("path", resourcePath);
                }
            }
        }
        return resolvedUrls;
    }

    @SuppressFBWarnings(value = "DMI_COLLECTION_OF_URLS", justification = "Those URLs are denoting local resources "
            + "without DNS resolution")
    private Set<URL> resolveWebClassesLocations() {
        final Set<URL> resolvedUrls = new HashSet<>();
        Set<String> resourcePaths = servletContext.getResourcePaths(WEB_INF_CLASSES);
        if (resourcePaths != null) {
            for (String resourcePath : resourcePaths) {
                if (resourcePath.startsWith(WEB_INF_CLASSES)) {
                    String suffix = resourcePath.substring(WEB_INF_CLASSES.length());
                    try {
                        URL resource = servletContext.getResource(resourcePath);
                        if (resource != null) {
                            String resourceAsString = resource.toExternalForm();
                            if (resourceAsString.endsWith(suffix)) {
                                resolvedUrls.add(new URL(resourceAsString.substring(0,
                                        resourceAsString.length() - suffix.length())));
                            } else {
                                LOGGER.warn("Ignoring invalid '{}' location: {}", WEB_INF_CLASSES, resourcePath);
                            }
                        } else {
                            LOGGER.warn("Ignoring unresolvable '{}' location: {}", WEB_INF_CLASSES, resourcePath);
                        }
                    } catch (Exception e) {
                        throw SeedException.wrap(e, WebErrorCode.CANNOT_RESOLVE_WEB_RESOURCE_LOCATION)
                                .put("path", resourcePath);
                    }
                } else {
                    LOGGER.warn("Ignoring invalid '{}' location: {}", WEB_INF_CLASSES, resourcePath);
                }
            }
        }
        return resolvedUrls;
    }
}
