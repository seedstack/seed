/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.internal;

import org.seedstack.shed.exception.ErrorCode;

public enum SecurityErrorCode implements ErrorCode {
    DUPLICATE_SCOPE_NAME,
    MISSING_ADEQUATE_SCOPE_CONSTRUCTOR,
    MULTIPLE_MAIN_SECURITY_MODULES,
    UNABLE_TO_CREATE_SCOPE,
    UNEXPECTED_ERROR
}
