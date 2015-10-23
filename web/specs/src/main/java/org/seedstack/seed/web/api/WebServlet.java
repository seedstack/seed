/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web.api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation declares a servlet class to be automatically registered as a web filter with the servlet
 * container. The servlet will be managed by Guice and can be injected.
 *
 * @author adrien.lauer@mpsa.com
 */
@Documented
@Target(value= ElementType.TYPE)
@Retention(value= RetentionPolicy.RUNTIME)
public @interface WebServlet {
    /**
     * @return name of the servlet.
     */
    String name() default "";

    /**
     * @return initialization parameters of the servlet (equivalent to an &lt;init-params&gt;&lt;/init-params&gt; section in the web.xml).
     */
    WebInitParam[] initParams() default {};

    /**
     * @return the url pattern(s) that the servlet is configured to serve.
     */
    String[] value() default {};
}
