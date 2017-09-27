/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.web.spi;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This service provides Cross-Site-Request-Forgery (CSRF or XSRF) protection to HTTP requests.
 */
public interface AntiXsrfService {
    void applyXsrfProtection(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse);

    void cleanXsrfProtection(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse);
}
