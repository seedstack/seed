/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal;

import org.seedstack.seed.ErrorCode;

public enum CoreErrorCode implements ErrorCode {
    ERROR_DURING_LIFECYCLE_CALLBACK,
    MISSING_CONFIGURATION_KEY,
    MISSING_SEED_ENTRY_POINT,
    MULTIPLE_SEED_ENTRY_POINTS,
    MULTIPLE_TOOLS_WITH_IDENTICAL_NAMES,
    NO_LOCAL_STORAGE_CONFIGURED,
    RETHROW_EXCEPTION_AFTER_DIAGNOSTIC_FAILURE,
    STORAGE_DIRECTORY_IS_NOT_WRITABLE,
    STORAGE_PATH_IS_NOT_A_DIRECTORY,
    TOOL_NOT_FOUND,
    UNABLE_TO_CREATE_PROXY,
    UNABLE_TO_CREATE_STORAGE_DIRECTORY,
    UNABLE_TO_FIND_CLASSLOADER,
    UNABLE_TO_INJECT_CONFIGURATION_VALUE,
    UNABLE_TO_INJECT_LOGGER,
    UNABLE_TO_INSTANTIATE_CLASS,
    UNABLE_TO_INSTANTIATE_MODULE,
    UNABLE_TO_LOAD_CONFIGURATION_RESOURCE,
    UNEXPECTED_EXCEPTION
}
