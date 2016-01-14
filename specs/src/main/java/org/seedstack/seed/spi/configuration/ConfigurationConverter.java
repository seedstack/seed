/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.spi.configuration;

/**
 * Convert a configuration value from a string to a specific type.
 *
 * @param <T> The output type of the conversion process.
 * @author adrien.lauer@mpsa.com
 */
public interface ConfigurationConverter<T> {
     
    /**
     * Converts string value provided by configuration to type expected by
     * annotated field. Each implementation must provide no-argument constructor
     * in order to be instantiated by injector.
     * @param value The configuration value as a String.
     * @return The converted configuration value.
     */
    T convert(String value);
}
