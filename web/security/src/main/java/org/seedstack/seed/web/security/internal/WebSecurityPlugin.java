/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web.security.internal;

import com.google.common.collect.Lists;
import com.google.inject.Module;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.PluginException;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.api.plugin.request.ClasspathScanRequest;
import io.nuun.kernel.core.AbstractPlugin;
import jodd.props.Props;
import org.apache.shiro.guice.web.GuiceShiroFilter;
import org.seedstack.seed.SeedRuntime;
import org.seedstack.seed.core.internal.application.ApplicationPlugin;
import org.seedstack.seed.security.internal.SecurityGuiceConfigurer;
import org.seedstack.seed.security.internal.SecurityProvider;
import org.seedstack.seed.web.security.SecurityFilter;
import org.seedstack.seed.web.spi.FilterDefinition;
import org.seedstack.seed.web.spi.ListenerDefinition;
import org.seedstack.seed.web.spi.SeedFilterPriority;
import org.seedstack.seed.web.spi.ServletDefinition;
import org.seedstack.seed.web.spi.WebProvider;

import javax.servlet.Filter;
import javax.servlet.ServletContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This plugins adds web security.
 *
 * @author yves.dautremay@mpsa.com
 */
public class WebSecurityPlugin extends AbstractPlugin implements SecurityProvider, WebProvider {
    private final Collection<Class<? extends Filter>> scannedFilters = new ArrayList<Class<? extends Filter>>();
    private ServletContext servletContext;
    private String applicationId;
    private Props props;

    @Override
    public String name() {
        return "web-security";
    }

    @Override
    public void provideContainerContext(Object containerContext) {
        servletContext = ((SeedRuntime) containerContext).contextAs(ServletContext.class);
    }

    @Override
    public Collection<Class<?>> requiredPlugins() {
        return Lists.<Class<?>>newArrayList(ApplicationPlugin.class);
    }

    @Override
    public Collection<ClasspathScanRequest> classpathScanRequests() {
        return classpathScanRequestBuilder().annotationType(SecurityFilter.class).build();
    }

    @SuppressWarnings("unchecked")
    @Override
    public InitState init(InitContext initContext) {
        ApplicationPlugin applicationPlugin = initContext.dependency(ApplicationPlugin.class);

        props = applicationPlugin.getProps();
        applicationId = applicationPlugin.getApplication().getId();

        for (Class<?> filterClass : initContext.scannedClassesByAnnotationClass().get(SecurityFilter.class)) {
            if (Filter.class.isAssignableFrom(filterClass)) {
                scannedFilters.add((Class<? extends Filter>) filterClass);
            } else {
                throw new PluginException("Annotated class " + filterClass.getName() + " must implement Filter to be used in a filter chain");
            }
        }

        return InitState.INITIALIZED;
    }

    @Override
    public Module provideMainSecurityModule(SecurityGuiceConfigurer securityGuiceConfigurer) {
        return servletContext != null ? new WebSecurityModule(
                servletContext,
                props,
                scannedFilters,
                applicationId,
                securityGuiceConfigurer
        ) : null;
    }

    @Override
    public Module provideAdditionalSecurityModule() {
        return null;
    }

    @Override
    public List<ServletDefinition> servlets() {
        return null;
    }

    @Override
    public List<FilterDefinition> filters() {
        FilterDefinition securityFilter = new FilterDefinition("security", GuiceShiroFilter.class);
        securityFilter.setPriority(SeedFilterPriority.SECURITY);
        securityFilter.setAsyncSupported(true);
        securityFilter.addMappings(new FilterDefinition.Mapping("/*"));
        return Lists.newArrayList(securityFilter);
    }

    @Override
    public List<ListenerDefinition> listeners() {
        return null;
    }
}