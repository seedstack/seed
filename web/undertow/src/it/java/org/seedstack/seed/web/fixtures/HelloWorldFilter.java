/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.web.fixtures;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebInitParam;
import org.seedstack.seed.Logging;
import org.slf4j.Logger;

@WebFilter(value = {"/helloFilter"}, initParams = {@WebInitParam(name = "param2", value = HelloWorldFilter
        .PARAM2_VALUE)})
public class HelloWorldFilter implements Filter {
    public static final String CONTENT = "Hello World!";
    public static final String PARAM2_VALUE = "value2";
    private static final long serialVersionUID = 1L;
    @Logging
    private Logger logger;
    private String param2;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        param2 = filterConfig.getInitParameter("param2");
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
            FilterChain filterChain) throws IOException {
        assertThat(logger).isNotNull();

        String text = CONTENT + " " + param2;

        servletResponse.setContentType("text/plain");
        servletResponse.setContentLength(text.length());

        servletResponse.getWriter().write(text);
    }

    @Override
    public void destroy() {

    }
}
