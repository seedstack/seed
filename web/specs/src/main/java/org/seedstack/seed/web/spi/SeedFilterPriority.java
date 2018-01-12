/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.web.spi;

/**
 * Holds priorities used by Seed filters.
 */
public class SeedFilterPriority {
    /**
     * Above all filters to enable usage of Web-specific injection scopes.
     */
    public static final int GUICE = Integer.MAX_VALUE;

    /**
     * Diagnostic filter is at the top to be able to catch all errors if enabled.
     */
    public static final int DIAGNOSTIC = 3000;

    /**
     * CORS filter is above security to be able to handle CORS pre-flight requests which don't have authentication
     * headers.
     */
    public static final int CORS = 2000;

    /**
     * Security is above all normal filters to be able to secure application content.
     */
    public static final int SECURITY = 1000;

    /**
     * Normal priority (can be used for filters serving the main content of the application).
     */
    public static final int NORMAL = 0;

    /**
     * Static resources are served after all normal filters.
     */
    public static final int RESOURCES = -1000;
}
