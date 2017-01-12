/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web.security.internal.shiro;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.web.env.EnvironmentLoaderListener;
import org.apache.shiro.web.env.WebEnvironment;
import org.apache.shiro.web.filter.mgt.FilterChainResolver;
import org.apache.shiro.web.mgt.WebSecurityManager;

import javax.inject.Named;
import javax.servlet.ServletContext;

@Singleton
class WebGuiceEnvironment implements WebEnvironment {
    private FilterChainResolver filterChainResolver;
    private ServletContext servletContext;
    private WebSecurityManager securityManager;

    @Inject
    WebGuiceEnvironment(FilterChainResolver filterChainResolver, @Named(ShiroWebModule.NAME) ServletContext servletContext, WebSecurityManager securityManager) {
        this.filterChainResolver = filterChainResolver;
        this.servletContext = servletContext;
        this.securityManager = securityManager;

        servletContext.setAttribute(EnvironmentLoaderListener.ENVIRONMENT_ATTRIBUTE_KEY, this);
    }

    public FilterChainResolver getFilterChainResolver() {
        return filterChainResolver;
    }

    public ServletContext getServletContext() {
        return servletContext;
    }

    public WebSecurityManager getWebSecurityManager() {
        return securityManager;
    }

    public SecurityManager getSecurityManager() {
        return securityManager;
    }
}
