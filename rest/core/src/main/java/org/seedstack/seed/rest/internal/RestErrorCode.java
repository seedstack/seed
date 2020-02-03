/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.internal;

import org.seedstack.shed.exception.ErrorCode;

/**
 * Enumerate all REST support errors.
 */
public enum RestErrorCode implements ErrorCode {
    CANNOT_MERGE_RESOURCES_WITH_DIFFERENT_DOC,
    CANNOT_MERGE_RESOURCE_WITH_DIFFERENT_REL,
    MULTIPLE_PATH_FOR_THE_SAME_REL,
    UNSUPPORTED_CACHE_POLICY
}
