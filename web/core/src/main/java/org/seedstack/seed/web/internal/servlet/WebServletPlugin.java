/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.web.internal.servlet;

import static org.seedstack.shed.misc.PriorityUtils.priorityOf;

import com.google.common.base.Strings;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.api.plugin.request.ClasspathScanRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebListener;
import javax.servlet.annotation.WebServlet;
import org.seedstack.seed.core.SeedRuntime;
import org.seedstack.seed.core.internal.AbstractSeedPlugin;
import org.seedstack.seed.web.internal.WebPlugin;
import org.seedstack.seed.web.spi.FilterDefinition;
import org.seedstack.seed.web.spi.ListenerDefinition;
import org.seedstack.seed.web.spi.ServletDefinition;
import org.seedstack.seed.web.spi.WebProvider;

/**
 * This plugin detects {@link WebServlet}, {@link WebFilter} and {@link WebListener} annotated classes and provides
 * their
 * corresponding definitions to {@link WebPlugin} for registration with the container.
 */
public class WebServletPlugin extends AbstractSeedPlugin implements WebProvider {
    private final List<FilterDefinition> filterDefinitions = new ArrayList<>();
    private final List<ServletDefinition> servletDefinitions = new ArrayList<>();
    private final List<ListenerDefinition> listenerDefinitions = new ArrayList<>();
    private ServletContext servletContext;

    @Override
    public String name() {
        return "web-servlet";
    }

    @Override
    public void setup(SeedRuntime seedRuntime) {
        servletContext = seedRuntime.contextAs(ServletContext.class);
    }

    @Override
    public Collection<ClasspathScanRequest> classpathScanRequests() {
        return classpathScanRequestBuilder()
                .annotationType(WebFilter.class)
                .annotationType(WebServlet.class)
                .annotationType(WebListener.class)
                .build();
    }

    @Override
    public InitState initialize(InitContext initContext) {
        if (servletContext != null) {
            listenerDefinitions.addAll(
                    detectListeners(initContext.scannedClassesByAnnotationClass().get(WebListener.class)));
            filterDefinitions.addAll(detectFilters(initContext.scannedClassesByAnnotationClass().get(WebFilter.class)));
            servletDefinitions.addAll(
                    detectServlets(initContext.scannedClassesByAnnotationClass().get(WebServlet.class)));
        }

        return InitState.INITIALIZED;
    }

    @SuppressWarnings("unchecked")
    private Collection<? extends ListenerDefinition> detectListeners(Collection<Class<?>> listenerClasses) {
        List<ListenerDefinition> listenerDefinitions = new ArrayList<>();
        for (Class<?> candidate : listenerClasses) {
            if (EventListener.class.isAssignableFrom(candidate)) {
                Class<? extends EventListener> listenerClass = (Class<? extends EventListener>) candidate;
                ListenerDefinition listenerDefinition = new ListenerDefinition(listenerClass);
                listenerDefinition.setPriority(priorityOf(listenerClass));
                listenerDefinitions.add(listenerDefinition);
            }
        }
        return listenerDefinitions;
    }

    @SuppressWarnings("unchecked")
    private List<FilterDefinition> detectFilters(Collection<Class<?>> filterClasses) {
        List<FilterDefinition> filterDefinitions = new ArrayList<>();
        for (Class<?> candidate : filterClasses) {
            if (Filter.class.isAssignableFrom(candidate)) {
                Class<? extends Filter> filterClass = (Class<? extends Filter>) candidate;
                WebFilter annotation = filterClass.getAnnotation(WebFilter.class);
                FilterDefinition filterDefinition = new FilterDefinition(
                        Strings.isNullOrEmpty(
                                annotation.filterName()) ? filterClass.getCanonicalName() : annotation.filterName(),
                        filterClass
                );
                filterDefinition.setAsyncSupported(annotation.asyncSupported());
                if (annotation.servletNames().length > 0) {
                    filterDefinition.addServletMappings(
                            convert(annotation.dispatcherTypes(), false, annotation.servletNames()));
                }
                if (annotation.value().length > 0) {
                    filterDefinition.addMappings(convert(annotation.dispatcherTypes(), false, annotation.value()));
                }
                if (annotation.urlPatterns().length > 0) {
                    filterDefinition.addMappings(
                            convert(annotation.dispatcherTypes(), false, annotation.urlPatterns()));
                }
                filterDefinition.addInitParameters(convert(annotation.initParams()));
                filterDefinition.setPriority(priorityOf(filterClass));

                filterDefinitions.add(filterDefinition);
            }
        }

        return filterDefinitions;
    }

    @SuppressWarnings("unchecked")
    private List<ServletDefinition> detectServlets(Collection<Class<?>> servletClasses) {
        List<ServletDefinition> servletDefinitions = new ArrayList<>();
        for (Class<?> candidate : servletClasses) {
            if (Servlet.class.isAssignableFrom(candidate)) {
                Class<? extends Servlet> servletClass = (Class<? extends Servlet>) candidate;
                WebServlet annotation = servletClass.getAnnotation(WebServlet.class);
                ServletDefinition servletDefinition = new ServletDefinition(
                        Strings.isNullOrEmpty(annotation.name()) ? servletClass.getCanonicalName() : annotation.name(),
                        servletClass
                );
                servletDefinition.setAsyncSupported(annotation.asyncSupported());
                if (annotation.value().length > 0) {
                    servletDefinition.addMappings(annotation.value());
                }
                if (annotation.urlPatterns().length > 0) {
                    servletDefinition.addMappings(annotation.urlPatterns());
                }
                servletDefinition.setLoadOnStartup(annotation.loadOnStartup());
                servletDefinition.addInitParameters(convert(annotation.initParams()));

                servletDefinitions.add(servletDefinition);
            }
        }

        return servletDefinitions;
    }

    private FilterDefinition.Mapping convert(DispatcherType[] dispatcherTypes, boolean isMatchAfter, String... values) {
        EnumSet<DispatcherType> enumSet = EnumSet.noneOf(DispatcherType.class);
        Collections.addAll(enumSet, dispatcherTypes);

        return new FilterDefinition.Mapping(enumSet, isMatchAfter, values);
    }

    private Map<String, String> convert(WebInitParam[] webInitParams) {
        Map<String, String> map = new HashMap<>();
        for (WebInitParam webInitParam : webInitParams) {
            map.put(webInitParam.name(), webInitParam.value());
        }
        return map;
    }

    @Override
    public Object nativeUnitModule() {
        return new WebServletModule(filterDefinitions, servletDefinitions, listenerDefinitions);
    }

    @Override
    public List<ServletDefinition> servlets() {
        return servletDefinitions;
    }

    @Override
    public List<FilterDefinition> filters() {
        return filterDefinitions;
    }

    @Override
    public List<ListenerDefinition> listeners() {
        return listenerDefinitions;
    }
}
