/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed;

import java.io.File;

/**
 * This class specifies an interface to the application global object which consists of:
 * <ul>
 * <li>The identity of the application (human readable name, unique identifier and instance identifier),</li>
 * <li>The application storage location,</li>
 * <li>The list of available environments,</li>
 * <li>The name of the current detected environment,</li>
 * <li>The current status of debug mode (enabled or disabled).</li>
 * </ul>
 *
 * @author adrien.lauer@mpsa.com
 */
public interface Application {
    /**
     * Get the full human-readable name of the application.
     *
     * @return The name of the application.
     */
    String getName();

    /**
     * Get the organization-wide unique identifier of the application.
     *
     * @return The identifier.
     */
    String getId();

    /**
     * Get the version of the application.
     *
     * @return The application version.
     */
    String getVersion();

    /**
     * Get the application storage location.
     *
     * @param context The storage context.
     * @return The file object denoting application storage directory.
     */
    File getStorageLocation(String context);

    /**
     * Return the DOT format of the application injector graph filtered by filter regular expression.
     *
     * @param filter the filtering regular expression.
     * @return the DOT formatted graph definition.
     */
    String getInjectionGraph(String filter);

    /**
     * Return the DOT format of the application injector graph unfiltered.
     *
     * @return the DOT formatted graph definition.
     */
    String getInjectionGraph();

    /**
     * Return the application global configuration.
     *
     * @return the configuration
     */
    org.apache.commons.configuration.Configuration getConfiguration();

    /**
     * Looks for eventual props configuration for a class.
     *
     * @return the configuration map
     */
    org.apache.commons.configuration.Configuration getConfiguration(Class<?> clazz);

    /**
     * Substitute any ${...} expression in the given string with the configuration values.
     *
     * @param value the string to substitute.
     * @return the substituted string.
     */
    String substituteWithConfiguration(String value);
}
