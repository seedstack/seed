/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation that marks classes and methods which should be intercepted and checked for subject role ownership.
 */
@Target({TYPE, METHOD})
@Retention(RUNTIME)
public @interface RequiresRoles {
    /**
     * @return the array of role names to check for.
     */
    String[] value();

    /**
     * @return the logical operator to use between multiple roles.
     */
    Logical logical() default Logical.AND;
}
