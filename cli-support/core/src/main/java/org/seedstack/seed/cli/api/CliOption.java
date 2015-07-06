/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.cli.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * This annotation marks a field as an option of the commandline.
 *
 * @author epo.jemba@ext.mpsa.com
 * @author adrien.lauer@mpsa.com
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
public @interface CliOption {
    /**
     * The short name of the option.
     */
    String name();

    /**
     * The long name of the option.
     */
    String longName() default "";

    /**
     * If the option is mandatory.
     */
    boolean mandatory() default false;

    /**
     * The number of values this option can take (or -1 if unlimited).
     */
    int valueCount() default 0;

    /**
     * The character for separating values.
     */
    char valueSeparator() default '=';

    /**
     * The default value(s) of the option.
     */
    String[] defaultValues() default {};

    /**
     * The description of the option (used by the help command).
     */
    String description() default "";
}