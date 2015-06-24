/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web.internal;

import org.seedstack.seed.core.api.DiagnosticManager;
import org.seedstack.seed.core.api.SeedException;
import org.seedstack.seed.web.api.WebErrorCode;

import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

class ExceptionDiagnosticFilter implements Filter {
    @Inject
    private DiagnosticManager diagnosticManager;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // nothing to do here
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            chain.doFilter(request, response);
        } catch (Exception e) {
            diagnosticManager.dumpDiagnosticReport(e);
            throw SeedException.wrap(e, WebErrorCode.UNEXPECTED_WEB_EXCEPTION);
        }
    }

    @Override
    public void destroy() {
        // nothing to do here
    }
}
