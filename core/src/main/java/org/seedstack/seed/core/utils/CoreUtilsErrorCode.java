/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.utils;

import org.seedstack.seed.core.api.ErrorCode;

/**
 * @author adrien.lauer@mpsa.com
 */
enum CoreUtilsErrorCode implements ErrorCode {
    PROPERTY_NOT_FOUND,
    UNABLE_TO_SET_PROPERTY,
    ANNOTATION_INJECTION_NOT_SUPPORTED,
    SATISFIED_BY_CHECK_FAILED, 
    INJECTABLE_CHECK_FAILED, 
    CHECK_FAILED, 
    NOT_NULL_CHECK_FAILED, 
    NULL_CHECK_FAILED, 
    ERROR_OCCURRED_TRYING_TO_INJECT_ANNOTATION,
    DUPLICATED_KEYS_FOUND,
    GET_FIELD_VALUE_FAILED,
    SET_FIELD_VALUE_FAILED,
    METHOD_INVOCATION_FAILED,
    ERROR_BUILDING_TUPLE
}
