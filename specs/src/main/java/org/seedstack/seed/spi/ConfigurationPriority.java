/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.spi;

public class ConfigurationPriority {
    /**
     * Runtime information has the highest priority to avoid any override.
     */
    public static final int RUNTIME_INFO = Integer.MAX_VALUE;

    /**
     * Environment variables have the highest priority to avoid any override.
     */
    public static final int SYSTEM_PROPERTIES = Integer.MAX_VALUE;

    /**
     * Environment variables have the highest priority to avoid any override.
     */
    public static final int ENVIRONMENT_VARIABLES = Integer.MAX_VALUE;

    /**
     * Configuration done through kernel parameters prefixed with "seedstack.config".
     */
    public static final int SYSTEM_PROPERTIES_CONFIG = 3000;

    /**
     * Configuration done through system properties prefixed with "seedstack.config".
     */
    public static final int KERNEL_PARAMETERS_CONFIG = 2000;

    /**
     * Base configuration override ("application.override.yaml", "application.override.yml", "application.override
     * .json").
     */
    public static final int BASE_OVERRIDE = 1000;

    /**
     * Base configuration ("application.yaml", "application.yml", "application.json").
     */
    public static final int BASE = 0;

    /**
     * Scanned configuration override ("META-INF/configuration/*.override.yaml", "META-INF/configuration/*.override
     * .yml",
     * "META-INF/configuration/*.override.json").
     */
    public static final int SCANNED_OVERRIDE = -1000;

    /**
     * Scanned configuration ("META-INF/configuration/*.yaml", "META-INF/configuration/*.yml",
     * "META-INF/configuration/*.json").
     */
    public static final int SCANNED = -2000;

    /**
     * Default configuration (overridden by anything else).
     */
    public static final int DEFAULT = Integer.MIN_VALUE;
}
