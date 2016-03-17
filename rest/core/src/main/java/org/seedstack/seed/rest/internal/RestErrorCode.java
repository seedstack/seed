/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.internal;

import org.seedstack.seed.ErrorCode;

/**
 * Enumerate all REST support errors.
 *
 * @author adrien.lauer@mpsa.com
 */
public enum RestErrorCode implements ErrorCode {
    MULTIPLE_PATH_FOR_THE_SAME_REL,
    UNSUPPORTED_CACHE_POLICY,
    CANNOT_MERGE_RESOURCE_WITH_DIFFERENT_REL,
    CANNOT_MERGE_RESOURCES_WITH_DIFFERENT_DOC,
    JAX_RS_FEATURE_NOT_FOUND
}
