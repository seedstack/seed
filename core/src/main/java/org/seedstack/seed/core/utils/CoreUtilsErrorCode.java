/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.utils;

import org.seedstack.seed.ErrorCode;


enum CoreUtilsErrorCode implements ErrorCode {
    CHECK_FAILED,
    DUPLICATED_KEYS_FOUND,
    GET_FIELD_VALUE_FAILED,
    INJECTABLE_CHECK_FAILED,
    METHOD_INVOCATION_FAILED,
    NOT_NULL_CHECK_FAILED,
    NULL_CHECK_FAILED,
    SATISFIED_BY_CHECK_FAILED,
    SET_FIELD_VALUE_FAILED
}
