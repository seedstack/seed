/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web.security.internal;

import org.apache.shiro.web.servlet.AdviceFilter;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.web.security.spi.AntiXsrfService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

class AntiXsrfFilter extends AdviceFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(AntiXsrfFilter.class);

    @Inject
    AntiXsrfService AntiXsrfService;

    @Override
    protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
        try {
            AntiXsrfService.applyXsrfProtection((HttpServletRequest) request, (HttpServletResponse) response);
            return true;
        } catch (SeedException e) {
            switch ((WebSecurityErrorCodes) e.getErrorCode()) {
                case MISSING_XSRF_HEADER:
                    ((HttpServletResponse) response).sendError(HttpServletResponse.SC_FORBIDDEN, "Missing XSRF protection token in the request");
                    return false;
                case MISSING_XSRF_COOKIE:
                    ((HttpServletResponse) response).sendError(HttpServletResponse.SC_FORBIDDEN, "Missing XSRF protection token cookie");
                    return false;
                case INVALID_XSRF_TOKEN:
                    ((HttpServletResponse) response).sendError(HttpServletResponse.SC_FORBIDDEN, "Request token does not match session token");
                    return false;
                default:
                    LOGGER.error("An error occurred when applying XSRF protection", e);
                    return false;
            }
        }
    }

    protected void postHandle(ServletRequest request, ServletResponse response) throws Exception {
        AntiXsrfService.cleanXsrfProtection((HttpServletRequest) request, (HttpServletResponse) response);
    }
}
