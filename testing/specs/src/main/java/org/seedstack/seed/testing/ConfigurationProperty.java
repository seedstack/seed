/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.testing;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specify a configuration property for the duration of the test.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(ConfigurationProperties.class)
@Documented
@Inherited
public @interface ConfigurationProperty {
    /**
     * The name of the configuration property.
     *
     * @return the name.
     */
    String name();

    /**
     * The value of the configuration property.
     *
     * @return the value.
     */
    String value();

    /**
     * The profiles this configuration property applies with.
     *
     * @return the configuration profiles.
     */
    String[] profiles() default {};
}
