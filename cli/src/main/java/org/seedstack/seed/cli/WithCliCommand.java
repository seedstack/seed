/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.cli;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Apply this annotation on a test method or a test class to execute a SeedStack CLI command with the specified
 * arguments. Standard SeedStack startup through SeedITRunner is disabled for the whole class test. Only methods with
 * this annotation (or all method if it is applied on the class) will start a SeedStack environment.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
@Inherited
public @interface WithCliCommand {
    /**
     * @return the name of the command to execute.
     */
    String command();

    /**
     * @return the expected return code.
     */
    int expectedStatusCode() default 0;
}
