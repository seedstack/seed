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
 * This annotation declares a servlet filter class to be automatically registered as a web filter with the servlet
 * container. The filter will be managed by Guice and can be injected.
 *
 * @author adrien.lauer@mpsa.com
 */
@Documented
@Target(value= ElementType.TYPE)
@Retention(value= RetentionPolicy.RUNTIME)
public @interface WebFilter {
    /**
     * @return Name of the servlet filter.
     */
    String filterName() default "";

    /**
     * @return Array of the filter initialization parameters (equivalent to an &lt;init-params&gt;&lt;/init-params&gt; section in the web.xml).
     */
    WebInitParam[] initParams() default {};

    /**
     * @return The url pattern(s) that the filter is configured to intercept.
     */
    String[] value() default {};
}
