/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.servlet.Filter;
import javax.servlet.ServletContext;
import org.apache.shiro.guice.web.GuiceShiroFilter;
import org.seedstack.seed.core.SeedRuntime;
import org.seedstack.seed.core.internal.AbstractSeedPlugin;
import org.seedstack.seed.core.internal.configuration.ConfigurationPlugin;
import org.seedstack.seed.security.internal.SecurityGuiceConfigurer;
import org.seedstack.seed.security.internal.SecurityProvider;
import org.seedstack.seed.web.SecurityFilter;
import org.seedstack.seed.web.security.WebSecurityConfig;
import org.seedstack.seed.web.spi.FilterDefinition;
import org.seedstack.seed.web.spi.ListenerDefinition;
import org.seedstack.seed.web.spi.SeedFilterPriority;
import org.seedstack.seed.web.spi.ServletDefinition;
import org.seedstack.seed.web.spi.WebProvider;

/**
 * This plugins adds web security.
 */
public class WebSecurityPlugin extends AbstractSeedPlugin implements SecurityProvider, WebProvider {
    private final Collection<Class<? extends Filter>> scannedFilters = new ArrayList<>();
    private ServletContext servletContext;
    private String applicationId;
    private WebSecurityConfig securityConfig;

    @Override
    public String name() {
        return "web-security";
    }

    @Override
    public void setup(SeedRuntime seedRuntime) {
        servletContext = seedRuntime.contextAs(ServletContext.class);
    }

    @Override
    public Collection<Class<?>> dependencies() {
        return Lists.newArrayList(ConfigurationPlugin.class);
    }

    @Override
    public Collection<ClasspathScanRequest> classpathScanRequests() {
        return classpathScanRequestBuilder().annotationType(SecurityFilter.class).build();
    }

    @SuppressWarnings("unchecked")
    @Override
    public InitState initialize(InitContext initContext) {
        ConfigurationPlugin configurationPlugin = initContext.dependency(ConfigurationPlugin.class);

        applicationId = configurationPlugin.getApplication().getId();
        securityConfig = getConfiguration(WebSecurityConfig.class);

        for (Class<?> filterClass : initContext.scannedClassesByAnnotationClass().get(SecurityFilter.class)) {
            if (Filter.class.isAssignableFrom(filterClass)) {
                scannedFilters.add((Class<? extends Filter>) filterClass);
            } else {
                throw new PluginException(
                        "Annotated class " + filterClass.getName() + " must implement Filter to be used in a filter "
                                + "chain");
            }
        }

        return InitState.INITIALIZED;
    }

    @Override
    public Module provideMainSecurityModule(SecurityGuiceConfigurer securityGuiceConfigurer) {
        return servletContext != null ? new WebSecurityModule(
                servletContext,
                securityConfig,
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