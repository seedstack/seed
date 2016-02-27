/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.seedstack.seed.web.security.internal.shiro;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.AnnotatedBindingBuilder;
import com.google.inject.name.Names;
import com.google.inject.servlet.ServletModule;
import org.apache.shiro.config.ConfigurationException;
import org.apache.shiro.env.Environment;
import org.apache.shiro.guice.ShiroModule;
import org.apache.shiro.guice.web.GuiceShiroFilter;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.web.env.WebEnvironment;
import org.apache.shiro.web.filter.PathMatchingFilter;
import org.apache.shiro.web.filter.authc.AnonymousFilter;
import org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter;
import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;
import org.apache.shiro.web.filter.authc.LogoutFilter;
import org.apache.shiro.web.filter.authc.UserFilter;
import org.apache.shiro.web.filter.authz.HttpMethodPermissionFilter;
import org.apache.shiro.web.filter.authz.PermissionsAuthorizationFilter;
import org.apache.shiro.web.filter.authz.PortFilter;
import org.apache.shiro.web.filter.authz.RolesAuthorizationFilter;
import org.apache.shiro.web.filter.authz.SslFilter;
import org.apache.shiro.web.filter.mgt.FilterChainResolver;
import org.apache.shiro.web.filter.session.NoSessionCreationFilter;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.mgt.WebSecurityManager;
import org.apache.shiro.web.session.mgt.ServletContainerSessionManager;

import javax.servlet.Filter;
import javax.servlet.ServletContext;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Sets up Shiro lifecycles within Guice, enables the injecting of Shiro objects, and binds a default
 * {@link org.apache.shiro.web.mgt.WebSecurityManager}, {@link org.apache.shiro.mgt.SecurityManager} and {@link org.apache.shiro.session.mgt.SessionManager}.  At least one realm must be added by
 * using {@link #bindRealm() bindRealm}.
 *
 * Also provides for the configuring of filter chains and binds a {@link org.apache.shiro.web.filter.mgt.FilterChainResolver} with that information.
 */
public abstract class ShiroWebModule extends ShiroModule {
    @SuppressWarnings({"UnusedDeclaration"})
    public static final Key<AnonymousFilter> ANON = Key.get(AnonymousFilter.class);
    @SuppressWarnings({"UnusedDeclaration"})
    public static final Key<FormAuthenticationFilter> AUTHC = Key.get(FormAuthenticationFilter.class);
    @SuppressWarnings({"UnusedDeclaration"})
    public static final Key<BasicHttpAuthenticationFilter> AUTHC_BASIC = Key.get(BasicHttpAuthenticationFilter.class);
    @SuppressWarnings({"UnusedDeclaration"})
    public static final Key<NoSessionCreationFilter> NO_SESSION_CREATION = Key.get(NoSessionCreationFilter.class);
    @SuppressWarnings({"UnusedDeclaration"})
    public static final Key<LogoutFilter> LOGOUT = Key.get(LogoutFilter.class);
    @SuppressWarnings({"UnusedDeclaration"})
    public static final Key<PermissionsAuthorizationFilter> PERMS = Key.get(PermissionsAuthorizationFilter.class);
    @SuppressWarnings({"UnusedDeclaration"})
    public static final Key<PortFilter> PORT = Key.get(PortFilter.class);
    @SuppressWarnings({"UnusedDeclaration"})
    public static final Key<HttpMethodPermissionFilter> REST = Key.get(HttpMethodPermissionFilter.class);
    @SuppressWarnings({"UnusedDeclaration"})
    public static final Key<RolesAuthorizationFilter> ROLES = Key.get(RolesAuthorizationFilter.class);
    @SuppressWarnings({"UnusedDeclaration"})
    public static final Key<SslFilter> SSL = Key.get(SslFilter.class);
    @SuppressWarnings({"UnusedDeclaration"})
    public static final Key<UserFilter> USER = Key.get(UserFilter.class);


    static final String NAME = "SHIRO";

    /**
     * We use a LinkedHashMap here to ensure that iterator order is the same as add order.  This is important, as the
     * FilterChainResolver uses iterator order when searching for a matching chain.
     */
    private final Map<String, FilterKey[]> filterChains = new LinkedHashMap<String, FilterKey[]>();
    private final ServletContext servletContext;

    public ShiroWebModule(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public static void bindGuiceFilter(Binder binder) {
        binder.install(guiceFilterModule());
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public static void bindGuiceFilter(final String pattern, Binder binder) {
        binder.install(guiceFilterModule(pattern));
    }

    public static ServletModule guiceFilterModule() {
        return guiceFilterModule("/*");
    }

    public static ServletModule guiceFilterModule(final String pattern) {
        return new ServletModule() {
            @Override
            protected void configureServlets() {
                filter(pattern).through(GuiceShiroFilter.class);
            }
        };
    }

    @Override
    protected final void configureShiro() {
        bindBeanType(TypeLiteral.get(ServletContext.class), Key.get(ServletContext.class, Names.named(NAME)));
        bind(Key.get(ServletContext.class, Names.named(NAME))).toInstance(this.servletContext);
        bindWebSecurityManager(bind(WebSecurityManager.class));
        bindWebEnvironment(bind(WebEnvironment.class));
        bind(GuiceShiroFilter.class).asEagerSingleton();
        expose(GuiceShiroFilter.class);

        this.configureShiroWeb();

        setupFilterChainConfigs();

        bind(FilterChainResolver.class).toProvider(new FilterChainResolverProvider(filterChains));
    }

    private void setupFilterChainConfigs() {
        Table<Key<? extends PathMatchingFilter>, String, String> configs = HashBasedTable.create();

        for (Map.Entry<String, FilterKey[]> filterChain : filterChains.entrySet()) {
            for (int i = 0; i < filterChain.getValue().length; i++) {
                FilterKey configKey = filterChain.getValue()[i];
                if (!configKey.getValue().isEmpty()) {
                    filterChain.getValue()[i] = configKey;
                    Key<? extends Filter> key = configKey.getKey();
                    if (!PathMatchingFilter.class.isAssignableFrom(key.getTypeLiteral().getRawType())) {
                        throw new ConfigurationException("Config information requires a PathMatchingFilter - can't apply to " + key.getTypeLiteral().getRawType());
                    }
                    configs.put(castToPathMatching(key), filterChain.getKey(), configKey.getValue());
                } else if (PathMatchingFilter.class.isAssignableFrom(configKey.getKey().getTypeLiteral().getRawType())) {
                    configs.put(castToPathMatching(configKey.getKey()), filterChain.getKey(), "");
                }
            }
        }
        for (Key<? extends PathMatchingFilter> filterKey : configs.rowKeySet()) {
            bindPathMatchingFilter(filterKey, configs.row(filterKey));
        }
    }

    private <T extends PathMatchingFilter> void bindPathMatchingFilter(Key<T> filterKey, Map<String, String> configs) {
        bind(filterKey).toProvider(new PathMatchingFilterProvider<T>(filterKey, configs)).asEagerSingleton();
    }

    @SuppressWarnings({"unchecked"})
    private Key<? extends PathMatchingFilter> castToPathMatching(Key<? extends Filter> key) {
        return (Key<? extends PathMatchingFilter>) key;
    }

    protected abstract void configureShiroWeb();

    @SuppressWarnings({"unchecked"})
    @Override
    protected final void bindSecurityManager(AnnotatedBindingBuilder<? super SecurityManager> bind) {
        bindWebSecurityManager(bind);
    }

    /**
     * Binds the security manager.  Override this method in order to provide your own security manager binding.
     *
     * By default, a {@link org.apache.shiro.web.mgt.DefaultWebSecurityManager} is bound as an eager singleton.
     *
     * @param bind the binding builder
     */
    protected void bindWebSecurityManager(AnnotatedBindingBuilder<? super WebSecurityManager> bind) {
        try {
            bind.toConstructor(DefaultWebSecurityManager.class.getConstructor(Collection.class)).asEagerSingleton();
        } catch (NoSuchMethodException e) {
            throw new ConfigurationException("This really shouldn't happen.  Either something has changed in Shiro, or there's a bug in ShiroModule.", e);
        }
    }

    /**
     * Binds the session manager.  Override this method in order to provide your own session manager binding.
     *
     * By default, a {@link org.apache.shiro.web.session.mgt.DefaultWebSessionManager} is bound as an eager singleton.
     *
     * @param bind the binding builder
     */
    @Override
    protected void bindSessionManager(AnnotatedBindingBuilder<SessionManager> bind) {
        bind.to(ServletContainerSessionManager.class).asEagerSingleton();
    }

    @Override
    protected final void bindEnvironment(AnnotatedBindingBuilder<Environment> bind) {
        bindWebEnvironment(bind);
    }

    protected void bindWebEnvironment(AnnotatedBindingBuilder<? super WebEnvironment> bind) {
        bind.to(WebGuiceEnvironment.class).asEagerSingleton();
    }

    /**
     * Adds a filter chain to the shiro configuration.
     *
     * NOTE: If the provided key is for a subclass of {@link org.apache.shiro.web.filter.PathMatchingFilter}, it will be registered with a proper
     * provider.
     *
     * @param pattern the pattern
     * @param keys the filter keys
     */
    @SuppressWarnings({"UnusedDeclaration"})
    protected final void addFilterChain(String pattern, FilterKey... keys) {
        filterChains.put(pattern, keys);
    }

    public static class FilterKey {
        private final Key<? extends Filter> key;
        private final String value;

        public FilterKey(Key<? extends Filter> key, String value) {
            this.key = key;
            this.value = value;
        }

        Key<? extends Filter> getKey() {
            return key;
        }

        String getValue() {
            return value;
        }
    }
}
