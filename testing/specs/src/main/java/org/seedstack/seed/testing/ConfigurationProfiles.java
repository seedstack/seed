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
 * Specify one or more configuration profile to be active for the duration of the test.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface ConfigurationProfiles {
    /**
     * The configuration profiles to be active during the test.
     *
     * @return the configuration profile list.
     */
    String[] value() default {};

    /**
     * If true, only when the annotation is placed on a test method and there is already another annotation on the
     * class, its profiles are appended to the class annotation instead of replacing them.
     *
     * @return if true, profiles are appended to the ones one the test class, otherwise they replace them.
     */
    boolean append() default true;
}
