/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.cli;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation marks a field as an option of the commandline. Field can be of type:
 * <ul>
 * <li>boolean in which case the option must take no value and the field will be set to true if the option
 * is present of false otherwise.</li>
 * <li>String in which case the option must take at least one value and the field will be set to the first
 * value of the option. If option is not present or has no value, the field will be set to null.</li>
 * <li>String[] in which case the option must take zero or more values and the field will be set to an array
 * containing all values of the option. If option is not present or has no value, the field will be set to
 * null.</li>
 * <li>Map&lt;String, String&gt; in which case the option must take an even number of values and the field
 * will be set to a map containing odd option values as keys and even option values as values. If option is
 * not present or has no value, the field will be set to an empty map.</li>
 * </ul>
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
     * The number of values this option can take (or -1 if unlimited). Checked only if the option is present in the
     * command line.
     */
    int valueCount() default 0;

    /**
     * The character for separating option values. It can be set to '=' to parse named option values. When providing
     * -Okey1=value1 -Okey2=value2 on the command line, the result can then be injected into a String array containing
     * key/value pairs ([ "key1", "value1", "key2", "value2"]) or directly into a Map&lt;String, String&gt;.
     */
    char valueSeparator() default ',';

    /**
     * If the option values are mandatory. Checked only if the option is present in the command line.
     */
    boolean mandatoryValue() default false;

    /**
     * The default value(s) of the option. Used when the option is not mandatory and not present in the command line.
     * Also used when the option is present, its values are not mandatory and are not present in the command line.
     */
    String[] defaultValues() default {};

    /**
     * The description of the option (used by the help command).
     */
    String description() default "";
}