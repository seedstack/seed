/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.spi.configuration;

import io.nuun.kernel.api.annotations.Facet;
import org.apache.commons.configuration.Configuration;

/**
 * This interface provides methods to access the application configuration.
 * <p>
 * It is exposed by the ApplicationPlugin to other plugins. It cannot be injected.
 * </p>
 *
 * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
 */
@Facet
public interface ConfigurationProvider {
    
    /**
     * Return the application global configuration.
     *
     * @return the configuration object.
     */
    Configuration getConfiguration();

    /**
     * Looks for eventual props configuration for a class.
     *
     * @return the configuration map
     */
    Configuration getConfiguration(Class<?> clazz);
}
