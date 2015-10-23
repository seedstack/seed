/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.application;

import org.seedstack.seed.core.api.ErrorCode;

/**
 * SEED application error codes.
 *
 * @author adrien.lauer@mpsa.com
 */
public enum ApplicationErrorCode implements ErrorCode {
    MISSING_APPLICATION_IDENTIFIER,
    STORAGE_PATH_IS_NOT_A_DIRECTORY,
    UNABLE_TO_CREATE_STORAGE_DIRECTORY,
    STORAGE_DIRECTORY_IS_NOT_WRITABLE,

    UNABLE_TO_GENERATE_INJECTION_GRAPH,

    UNABLE_TO_LOAD_CONFIGURATION_RESOURCE,
    UNABLE_TO_INSTANTIATE_CONFIGURATION_ARRAY,
    CONFIGURATION_ERROR,
    CONVERTER_NOT_COMPATIBLE,
    CONVERTER_INSTANTIATION,
    CONVERTER_CONSTRUCTOR_ILLEGAL_ACCESS,
    NO_SUITABLE_CONFIGURATION_LOOKUP_CONSTRUCTOR_FOUND, UNABLE_TO_INSTANTIATE_CONFIGURATION_LOOKUP, FIELD_ILLEGAL_ACCESS
}
