/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.jersey2.fixtures.filters;

import org.seedstack.seed.rest.jersey2.fixtures.FilteredResource;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Filter class using default priority. Updates to the request are expected to be visible in resource class.
 */
@javax.servlet.annotation.WebFilter("/filter/*")
public class DefaultPriorityWebFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        req.setAttribute(FilteredResource.OVERRIDDEN_ATTRIBUTE_NAME, "DefaultPriorityWebFilter");

        chain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // nothing to do
    }

    @Override
    public void destroy() {
        // nothing to do
    }
}
