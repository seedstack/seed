/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;

/**
 * This can be used to specify a named JNDI context when applied on a @Resource annotated field.
 *
 * @author adrien.lauer@mpsa.com
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ TYPE, FIELD, METHOD })
@Inherited
public @interface FromContext {
    /**
     * The name of the context to use for resource lookup.
     */
    String value() default "";
}
