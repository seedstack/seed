/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.el;

import org.seedstack.shed.exception.ErrorCode;

enum ELErrorCode implements ErrorCode {
    NO_METHOD_VALUE_AVAILABLE,
    PROPERTY_NOT_FOUND,
    EL_EXCEPTION,
    EL_ANNOTATION_IS_ALREADY_BIND
}
