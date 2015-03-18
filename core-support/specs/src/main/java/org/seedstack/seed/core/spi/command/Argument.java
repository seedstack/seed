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
 * This annotation marks fields of a command to be injected with the argument value set at command invocation.
 *
 * @author adrien.lauer@mpsa.com
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Argument {

    /**
     * The index of the argument.
     */
    int index();

    /**
     * The name of the argument.
     */
    String name() default "";

    /**
     * If the argument is mandatory.
     */
    boolean mandatory() default true;

    /**
     * The default value to use if argument is not present.
     */
    String defaultValue() default "";

    /**
     * The description of the argument (used by the help command).
     */
    String description() default "";
}
