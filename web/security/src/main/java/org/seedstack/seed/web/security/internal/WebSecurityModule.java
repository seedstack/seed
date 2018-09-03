/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.web.security.internal;

import com.google.common.base.Strings;
import com.google.inject.Key;
import com.google.inject.Scopes;
import com.google.inject.name.Names;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.Filter;
import javax.servlet.ServletContext;
import org.apache.shiro.config.ConfigurationException;
import org.apache.shiro.guice.web.GuiceShiroFilter;
import org.apache.shiro.guice.web.ShiroWebModule;
import org.apache.shiro.util.StringUtils;
import org.apache.shiro.web.filter.PathMatchingFilter;
import org.apache.shiro.web.filter.authc.LogoutFilter;
import org.apache.shiro.web.filter.authc.SeedBasicHttpAuthenticationFilter;
import org.apache.shiro.web.filter.authc.SeedFormAuthenticationFilter;
import org.seedstack.seed.security.internal.SecurityGuiceConfigurer;
import org.seedstack.seed.web.SecurityFilter;
import org.seedstack.seed.web.security.WebSecurityConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class WebSecurityModule extends ShiroWebModule {
    private static final Logger LOGGER = LoggerFactory.getLogger(ShiroWebModule.class);
    private static final Map<String, Key<? extends Filter>> DEFAULT_FILTERS = new HashMap<>();

    static {
        // Shiro filters
        DEFAULT_FILTERS.put("anon", ANON);
        DEFAULT_FILTERS.put("logout", LOGOUT);
        DEFAULT_FILTERS.put("noSessionCreation", NO_SESSION_CREATION);
        DEFAULT_FILTERS.put("perms", PERMS);
        DEFAULT_FILTERS.put("port", PORT);
        DEFAULT_FILTERS.put("rest", REST);
        DEFAULT_FILTERS.put("roles", ROLES);
        DEFAULT_FILTERS.put("ssl", SSL);
        DEFAULT_FILTERS.put("user", USER);

        // SeedStack filters (authc and authcBasic prevent potential session fixation vulnerability)
        DEFAULT_FILTERS.put("authc", Key.get(SeedFormAuthenticationFilter.class));
        DEFAULT_FILTERS.put("authcBasic", Key.get(SeedBasicHttpAuthenticationFilter.class));
        DEFAULT_FILTERS.put("cert", Key.get(X509CertificateFilter.class));
        DEFAULT_FILTERS.put("xsrf", Key.get(AntiXsrfFilter.class));
    }

    private final String applicationName;
    private final WebSecurityConfig securityConfig;
    private final Collection<Class<? extends Filter>> customFilters;
    private final SecurityGuiceConfigurer securityGuiceConfigurer;

    WebSecurityModule(ServletContext servletContext, WebSecurityConfig securityConfig,
            Collection<Class<? extends Filter>> customFilters, String applicationName,
            SecurityGuiceConfigurer securityGuiceConfigurer) {
        super(servletContext);
        this.securityConfig = securityConfig;
        this.customFilters = customFilters;
        this.applicationName = applicationName;
        this.securityGuiceConfigurer = securityGuiceConfigurer;
    }

    @Override
    protected void configureShiroWeb() {
        for (WebSecurityConfig.UrlConfig urlConfig : securityConfig.getUrls()) {
            String pattern = urlConfig.getPattern();
            List<String> filters = urlConfig.getFilters();
            LOGGER.trace("Binding {} to security filter chain {}", pattern, filters);
            addFilterChain(pattern, getFilterKeys(filters));
        }
        LOGGER.debug("{} URL(s) bound to security filters", securityConfig.getUrls().size());

        // Bind filters which are not PatchMatchingFilters
        bind(LogoutFilter.class);

        // Bind custom filters not extending PathMatchingFilter as Shiro doesn't do it
        for (Class<? extends Filter> customFilter : customFilters) {
            if (!PathMatchingFilter.class.isAssignableFrom(customFilter)) {
                bind(customFilter);
            }
        }

        // General configuration attributes
        bindConstant().annotatedWith(Names.named("shiro.applicationName")).to(applicationName);
        bindConstant().annotatedWith(Names.named("shiro.loginUrl")).to(securityConfig.getLoginUrl());
        bindConstant().annotatedWith(Names.named("shiro.redirectUrl")).to(securityConfig.getLogoutUrl());
        bindConstant().annotatedWith(Names.named("shiro.successUrl")).to(securityConfig.getSuccessUrl());

        // Form configuration attributes
        WebSecurityConfig.FormConfig formConfig = securityConfig.form();
        bindConstant().annotatedWith(Names.named("shiro.usernameParam")).to(formConfig.getUsernameParameter());
        bindConstant().annotatedWith(Names.named("shiro.passwordParam")).to(formConfig.getPasswordParameter());
        bindConstant().annotatedWith(Names.named("shiro.rememberMeParam")).to(formConfig.getRememberMeParameter());
        bindConstant().annotatedWith(Names.named("shiro.failureAttribute")).to(formConfig.getFailureAttribute());

        // Shiro global configuration
        securityGuiceConfigurer.configure(binder());

        // Shiro filter
        bind(GuiceShiroFilter.class).in(Scopes.SINGLETON);
    }

    @SuppressWarnings("unchecked")
    private FilterConfig<?>[] getFilterKeys(List<String> filters) {
        FilterConfig<?>[] filterConfig = new FilterConfig<?>[filters.size()];
        int index = 0;
        for (String filter : filters) {
            String[] nameConfig = toNameConfigPair(filter);
            Key<? extends Filter> currentKey = findKey(nameConfig[0]);
            if (currentKey != null) {
                if (!Strings.isNullOrEmpty(nameConfig[1])) {
                    filterConfig[index] = filterConfig(currentKey, nameConfig[1]);
                } else {
                    filterConfig[index] = filterConfig(currentKey);
                }
            } else {
                addError(
                        "The filter [" + nameConfig[0] + "] could not be found as a default filter or as a class "
                                + "annotated with SecurityFilter");
            }
            index++;
        }
        return filterConfig;
    }

    private Key<? extends Filter> findKey(String filterName) {
        Key<? extends Filter> currentKey = null;
        if (DEFAULT_FILTERS.containsKey(filterName)) {
            currentKey = DEFAULT_FILTERS.get(filterName);
        } else {
            for (Class<? extends Filter> filterClass : customFilters) {
                String name = filterClass.getAnnotation(SecurityFilter.class).value();
                if (filterName.equals(name)) {
                    currentKey = Key.get(filterClass);
                }
            }
        }
        return currentKey;
    }

    /**
     * This method is copied from the same method in Shiro in class DefaultFilterChainManager.
     */
    private String[] toNameConfigPair(String token) throws ConfigurationException {
        String[] pair = token.split("\\[", 2);
        String name = StringUtils.clean(pair[0]);

        if (name == null) {
            throw new IllegalArgumentException("Filter name not found for filter chain definition token: " + token);
        }
        String config = null;

        if (pair.length == 2) {
            config = StringUtils.clean(pair[1]);
            //if there was an open bracket, it assumed there is a closing bracket, so strip it too:
            config = config.substring(0, config.length() - 1);
            config = StringUtils.clean(config);

            //backwards compatibility prior to implementing SHIRO-205:
            //prior to SHIRO-205 being implemented, it was common for end-users to quote the config inside brackets
            //if that config required commas.  We need to strip those quotes to get to the interior quoted definition
            //to ensure any existing quoted definitions still function for end users:
            if (config != null && config.startsWith("\"") && config.endsWith("\"")) {
                String stripped = config.substring(1, config.length() - 1);
                stripped = StringUtils.clean(stripped);

                //if the stripped value does not have any internal quotes, we can assume that the entire config was
                //quoted and we can use the stripped value.
                if (stripped != null && stripped.indexOf('"') == -1) {
                    config = stripped;
                }
                //else:
                //the remaining config does have internal quotes, so we need to assume that each comma delimited
                //pair might be quoted, in which case we need the leading and trailing quotes that we stripped
                //So we ignore the stripped value.
            }
        }

        return new String[]{name, config};

    }
}
