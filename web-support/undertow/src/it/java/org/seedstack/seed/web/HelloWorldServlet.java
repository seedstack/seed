/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web;


import org.seedstack.seed.web.api.WebInitParam;
import org.seedstack.seed.web.api.WebServlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
 */
@WebServlet(value = {"/hello"}, initParams = {@WebInitParam(name = "param1", value = HelloWorldServlet.PARAM1_VALUE)})
public class HelloWorldServlet  extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public static final String CONTENT = "Hello World!";
    public static final String PARAM1_VALUE = "value1";

    @Override
    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        String text = CONTENT + " " + getServletConfig().getInitParameter("param1");

        httpServletResponse.setContentType("text/plain");
        httpServletResponse.setContentLength(text.length());

        httpServletResponse.getWriter().write(text);
    }
}
