/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.spi.configuration;

import org.apache.commons.lang.text.StrLookup;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks {@link StrLookup} classes to be recognized and registered in Seed configuration
 *
 * @author adrien.lauer@mpsa.com
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE})
public @interface ConfigurationLookup {
    /**
     * @return the configuration lookup name (used in ${lookup-name:value} expressions).
     */
    String value();
}
