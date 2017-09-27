/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.web;

import javax.servlet.ServletContext;

/**
 * Factory to create a {@link WebResourceResolver}.
 */
public interface WebResourceResolverFactory {
    /**
     * Creates a WebResourceResolver.
     *
     * @param servletContext the servlet context to do resolution against.
     * @return the resource resolver.
     */
    WebResourceResolver createWebResourceResolver(ServletContext servletContext);
}
