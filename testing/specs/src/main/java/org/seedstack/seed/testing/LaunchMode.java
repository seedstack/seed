/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.testing;

/**
 * Enumeration of testing environment launch modes.
 */
public enum LaunchMode {
    /**
     * With this launch mode, a unique SeedStack environment is used for the whole test class.
     */
    PER_TEST_CLASS,
    /**
     * With this launch mode, a different SeedStack environment is used for each test.
     */
    PER_TEST,
    /**
     * With this launch mode, no SeedStack environment is used for any test.
     */
    NONE,
    /**
     * This denotes that no particular launch mode is requested.
     */
    ANY
}
