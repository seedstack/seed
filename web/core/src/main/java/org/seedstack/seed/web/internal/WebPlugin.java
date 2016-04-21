/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web.internal;

import com.google.common.collect.Lists;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceFilter;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.Context;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.core.AbstractPlugin;
import org.reflections.util.ClasspathHelper;
import org.seedstack.seed.SeedRuntime;
import org.seedstack.seed.web.spi.FilterDefinition;
import org.seedstack.seed.web.spi.ListenerDefinition;
import org.seedstack.seed.web.spi.ServletDefinition;
import org.seedstack.seed.web.spi.WebProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This plugin provides support for servlet-based Web applications.
 *
 * @author adrien.lauer@mpsa.com
 */
public class WebPlugin extends AbstractPlugin {
    public static final String WEB_PLUGIN_PREFIX = "org.seedstack.seed.web";
    private static final Logger LOGGER = LoggerFactory.getLogger(WebPlugin.class);

    private final List<FilterDefinition> filterDefinitions = new ArrayList<FilterDefinition>();
    private final List<ServletDefinition> servletDefinitions = new ArrayList<ServletDefinition>();
    private final List<ListenerDefinition> listenerDefinitions = new ArrayList<ListenerDefinition>();
    private ServletContext servletContext;

    @Override
    public String name() {
        return "web";
    }

    @Override
    public void provideContainerContext(Object containerContext) {
        servletContext = ((SeedRuntime) containerContext).contextAs(ServletContext.class);
    }

    @Override
    public Set<URL> computeAdditionalClasspathScan() {
        Set<URL> additionalUrls = new HashSet<URL>();

        if (servletContext != null) {
            // resource paths for WEB-INF/lib can be null when SEED run in the Undertow servlet container
            if (servletContext.getResourcePaths("/WEB-INF/lib") != null) {
                additionalUrls.addAll(ClasspathHelper.forWebInfLib(servletContext));
            }
            URL webInfClasses = ClasspathHelper.forWebInfClasses(servletContext);
            if (webInfClasses != null) {
                additionalUrls.add(webInfClasses);
            }
        }

        LOGGER.debug("{} additional URL(s) to scan were determined from Web classpath", additionalUrls.size());

        return additionalUrls;
    }

    @Override
    public Collection<Class<?>> requiredPlugins() {
        return Lists.<Class<?>>newArrayList(WebProvider.class);
    }

    @Override
    public InitState init(InitContext initContext) {
        if (servletContext != null) {
            List<WebProvider> webProviders = initContext.dependencies(WebProvider.class);

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
            Collections.sort(filterDefinitions, Collections.reverseOrder(new Comparator<FilterDefinition>() {
                @Override
                public int compare(FilterDefinition o1, FilterDefinition o2) {
                    return new Integer(o1.getPriority()).compareTo(o2.getPriority());
                }
            }));

            // Sort listeners according to the priority in their definition
            Collections.sort(listenerDefinitions, Collections.reverseOrder(new Comparator<ListenerDefinition>() {
                @Override
                public int compare(ListenerDefinition o1, ListenerDefinition o2) {
                    return new Integer(o1.getPriority()).compareTo(o2.getPriority());
                }
            }));
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
            ServletContextConfigurer servletContextConfigurer = new ServletContextConfigurer(servletContext, context.applicationObjectGraph().as(Injector.class));

            for (ListenerDefinition listenerDefinition : listenerDefinitions) {
                LOGGER.trace("Registering servlet listener {}", listenerDefinition.getListenerClass());
                servletContextConfigurer.addListener(listenerDefinition);
            }
            LOGGER.debug("Registered {} servlet listener(s)", listenerDefinitions.size());

            LOGGER.trace("Registering Guice servlet filter");
            servletContextConfigurer.addFilter(buildGuiceFilterDefinition());
            for (FilterDefinition filterDefinition : filterDefinitions) {
                LOGGER.trace("Registering servlet filter {} with {} priority", filterDefinition.getFilterClass(), filterDefinition.getPriority());
                servletContextConfigurer.addFilter(filterDefinition);
            }
            LOGGER.debug("Registered {} servlet filter(s)", filterDefinitions.size() + 1);

            for (ServletDefinition servletDefinition : servletDefinitions) {
                LOGGER.trace("Registering servlet {}", servletDefinition.getServletClass());
                servletContextConfigurer.addServlet(servletDefinition);
            }
            LOGGER.debug("Registered {} servlet(s)", servletDefinitions.size());
        }
    }

    private FilterDefinition buildGuiceFilterDefinition() {
        FilterDefinition guiceFilter = new FilterDefinition("guice", GuiceFilter.class);
        guiceFilter.setAsyncSupported(true);
        guiceFilter.addMappings(new FilterDefinition.Mapping("/*"));
        return guiceFilter;
    }
}
