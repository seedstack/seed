/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation marks Guice modules that will be detected and automatically installed.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
public @interface Install {
    /**
     * If true the module will be installed as an overriding module, meaning that every binding defined in it will
     * potentially override (replace) any similar binding already defined. If false, the module will be installed as
     * a normal one.
     *
     * @return if true the module is an overriding module, if false a normal module.
     */
    boolean override() default false;
}
