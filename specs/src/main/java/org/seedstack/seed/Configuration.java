/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed;

import org.seedstack.seed.spi.configuration.ConfigurationConverter;
import org.seedstack.seed.spi.configuration.ConfigurationIdentityConverter;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation marks fields which will be automatically valued by SEED, using application configuration.
 *
 * @author epo.jemba@ext.mpsa.com
 * @author adrien.lauer@mpsa.com
 * @author yves.dautremay@mpsa.com
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD})
public @interface Configuration {
    /**
     * Provides the default configuration error code.
     */
    enum ConfigurationErrorCode implements ErrorCode {
        CONFIGURATION_ERROR
    }

    /**
     * The configuration property name.
     */
    String value();

    /**
     * If the configuration property is mandatory.
     */
    boolean mandatory() default true;

    /**
     * The default value if configuration property is not present.
     */
    String[] defaultValue() default {};

    /**
     * The converter (string to any object) to use to convert the configuration property value.
     */
    Class<? extends ConfigurationConverter<?>> converter() default ConfigurationIdentityConverter.class;

    /**
     * The class of the {@link ErrorCode} to use in the SeedException thrown if a configuration error occurs.
     */
    Class<? extends Enum<? extends ErrorCode>> errorCodeClass() default ConfigurationErrorCode.class;

    /**
     * The code name to use in the SeedException thrown if a configuration error occurs.
     */
    String errorCodeName() default "CONFIGURATION_ERROR";
}
