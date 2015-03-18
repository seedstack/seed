/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.validation.internal;


import org.seedstack.seed.core.api.ErrorCode;

/**
 * Enumerates all validation error codes.
 *
 * @author epo.jemba@ext.mpsa.com
 * @author adrien.lauer@mpsa.com
 */
enum ValidationErrorCode implements ErrorCode {
    VALIDATION_ISSUE,
    DYNAMIC_VALIDATION_IS_NOT_SUPPORTED
}
