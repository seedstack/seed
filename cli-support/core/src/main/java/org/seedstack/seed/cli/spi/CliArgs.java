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
import io.nuun.plugin.cli.NuunArgs;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;





/**
 * This annotation mark a field as the args of the commandline.
 * <p>
 * Type must be an array of String? If a converter is provided an array of the converted class.
 *
 * @author epo.jemba@ext.mpsa.com
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD , ElementType.ANNOTATION_TYPE })
@NuunArgs
public @interface CliArgs {

    /**
     * @return true if the argument is mandatory, false otherwise.
     */
    boolean mandatory () default false;
    
    /**
     * @return a converter that will allow transformation from String to Something else 
     */
    Class<? extends NuunConfigurationConverter<?>> converter() default NuunDummyConverter.class;

}