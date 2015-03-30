/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.cli.spi;

import io.nuun.kernel.spi.configuration.NuunConfigurationConverter;
import io.nuun.kernel.spi.configuration.NuunDummyConverter;
import io.nuun.plugin.cli.NuunOption;
import org.apache.commons.cli.Option;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;




/**
 * This annotation mark a field as an option of the commandline.
 *
 * @author epo.jemba@ext.mpsa.com
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD , ElementType.ANNOTATION_TYPE })
@NuunOption
public @interface CliOption {
    /**
     * @return the identification string of the Option.
     */
    String opt() default "";

    /**
     * @return an alias and more descriptive identification string
     */
    String longOpt() default "";

    /**
     * @return a description of the function of the option
     */
    String description() default "";

    /**
     * @return a flag to say whether the option must appear on the command line.
     */
    boolean required() default false;

    /**
     * @return a flag to say whether the option takes an argument
     */
    boolean arg() default true;

    /**
     * @return a flag to say whether the option takes more than one argument
     */
    boolean args() default false;
    
    /**
     * @return explicit number of args for this option. 
     */
    int numArgs() default Option.UNINITIALIZED;

    /**
     * @return a flag to say whether the option's argument is optional
     */
    boolean optionalArg() default false;

    /**
     * @return the name of the argument value for the usage statement
     */
    String argName() default "";

    /**
     * @return the character value used to split the argument string, that is used in conjunction with
     *         multipleArgs e.g. if the separator is ',' and the argument string is 'a,b,c' then there are
     *         three argument values, 'a', 'b' and 'c'.
     */
    char valueSeparator() default '=';
    
    /**
     * @return a converter that will allow transformation from String to Something else 
     */
    Class<? extends NuunConfigurationConverter<?>> converter() default NuunDummyConverter.class;
}