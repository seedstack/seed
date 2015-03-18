/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.utils;

import org.apache.commons.configuration.Configuration;

import java.util.Iterator;
import java.util.Properties;

/**
 * Class with various utility methods for reading configuration.
 *
 * @author adrien.lauer@mpsa.com
 */
public final class SeedConfigurationUtils {

    private SeedConfigurationUtils() {
    }

    /**
     * Creates a Properties object from a commons configuration object using a specified property prefix.
     * @param configuration the commons configuration object
     * @param propertyPrefix the property prefix to search from.
     * @return the properties object containing the configuration subset derived from propertyPrefix.
     */
    public static Properties buildPropertiesFromConfiguration(Configuration configuration, final String propertyPrefix) {
        Properties properties = new Properties();
        Iterator<String> it = configuration.getKeys(propertyPrefix);
        while (it.hasNext()) {
            String name = it.next();
            properties.put(name.substring(propertyPrefix.length() + 1), configuration.getString(name));
        }

        return properties;
    }
}
