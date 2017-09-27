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
import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.seedstack.seed.Logging;
import org.slf4j.Logger;

@WebServlet(value = {"/hello"}, initParams = {@WebInitParam(name = "param1", value = HelloWorldServlet.PARAM1_VALUE)})
public class HelloWorldServlet extends HttpServlet {
    public static final String CONTENT = "Hello World!";
    public static final String PARAM1_VALUE = "value1";
    private static final long serialVersionUID = 1L;
    @Logging
    private Logger logger;

    @Override
    protected void doGet(HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse) throws ServletException, IOException {
        assertThat(logger).isNotNull();

        String text = CONTENT + " " + getServletConfig().getInitParameter("param1");

        httpServletResponse.setContentType("text/plain");
        httpServletResponse.setContentLength(text.length());

        httpServletResponse.getWriter().write(text);
    }
}
