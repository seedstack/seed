/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
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
import org.seedstack.seed.spi.SeedLauncher;

/**
 * This annotation can be used to specify the {@link SeedLauncher} used to launch the tested application.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface Expected {
    /**
     * The exception class expected to occur during the test.
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
