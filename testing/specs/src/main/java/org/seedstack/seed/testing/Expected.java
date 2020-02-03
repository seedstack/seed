/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.testing;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation can be used to specify the expected error conditions to occur during the launch of the tested
 * application.
 * <p>It cannot be used to specify the expected error conditions to occur during the test itself.</p>
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface Expected {
    /**
     * The exception class expected to occur during the tested application launch.
     *
     * @return the exception class.
     */
    Class<? extends Exception> value() default None.class;

    class None extends Exception {
        private static final long serialVersionUID = 1L;

        private None() {
        }
    }
}
