/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.undertow.fixtures;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(value = {"/hello"})
public class HelloWorldServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse) throws IOException {
        String text = "Hello World!";
        httpServletResponse.setContentType("text/plain");
        httpServletResponse.setContentLength(text.length());
        httpServletResponse.getWriter().write(text);
    }
}
