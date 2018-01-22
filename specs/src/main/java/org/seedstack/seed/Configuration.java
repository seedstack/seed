/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation marks fields which will be automatically valued by SEED, using application configuration.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Configuration {
    /**
     * @return the configuration property name.
     */
    String[] value() default {};

    /**
     * @return true if the configuration property is mandatory, false otherwise.
     */
    boolean mandatory() default false;

    /**
     * @return true if a default value should be injected when the field is null, false if the field should be left
     * as-is.
     */
    boolean injectDefault() default true;
}
