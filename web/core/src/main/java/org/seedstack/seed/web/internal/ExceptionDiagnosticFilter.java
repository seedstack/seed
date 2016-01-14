/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web.internal;

import org.seedstack.seed.DiagnosticManager;
import org.seedstack.seed.SeedException;

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
        } catch (SeedException e) {
            diagnosticManager.dumpDiagnosticReport(e);
            throw e;
        } catch (Exception e) {
            diagnosticManager.dumpDiagnosticReport(e);
            throw SeedException.wrap(e, WebErrorCode.UNEXPECTED_EXCEPTION);
        }
    }

    @Override
    public void destroy() {
        // nothing to do here
    }
}
