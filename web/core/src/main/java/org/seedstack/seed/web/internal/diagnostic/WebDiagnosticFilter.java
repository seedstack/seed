/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web.internal.diagnostic;

import org.seedstack.seed.core.Seed;
import org.seedstack.seed.diagnostic.DiagnosticManager;
import org.seedstack.seed.web.internal.ServletContextUtils;
import org.seedstack.shed.exception.BaseException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

public class WebDiagnosticFilter implements Filter {
    private DiagnosticManager diagnosticManager;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        diagnosticManager = ServletContextUtils.getInjector(filterConfig.getServletContext()).getInstance(DiagnosticManager.class);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            chain.doFilter(request, response);
        } catch (Exception e) {
            BaseException translated = Seed.translateException(e);
            diagnosticManager.dumpDiagnosticReport(translated);
            throw translated;
        }
    }

    @Override
    public void destroy() {
        // nothing to do here
    }
}
