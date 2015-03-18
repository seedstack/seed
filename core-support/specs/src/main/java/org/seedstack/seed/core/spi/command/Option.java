/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.spi.command;


import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation marks fields of a command to be injected with the option value set at command invocation.
 *
 * @author adrien.lauer@mpsa.com
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Option {
    /**
     * The short name of the option.
     */
    String name();

    /**
     * If the option has an argument (i.e. value).
     */
    boolean hasArgument() default false;

    /**
     * The long name of the option.
     */
    String longName() default "";

    /**
     * The description of the option (used by the help command).
     */
    String description() default "";

    /**
     * If the command is mandatory.
     */
    boolean mandatory() default false;

    /**
     * The default value of the option.
     */
    String defaultValue() default "";
}
