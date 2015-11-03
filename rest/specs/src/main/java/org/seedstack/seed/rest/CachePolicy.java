/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest;

/**
 * This enumeration lists all possible resource cache policies and can be used as a value of {@link CacheControl}.
 *
 * @author adrien.lauer@mpsa.com
 */
public enum CachePolicy {
    /**
     * The response should not be cached at all by the client.
     */
    NO_CACHE,

    /**
     * The response cache attributes are left untouched and may be set by applicative code.
     */
    CUSTOM
}
