/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.it.api;

import org.seedstack.seed.core.api.ErrorCode;

/**
 * Enumerates all IT error codes.
 *
 * @author adrien.lauer@mpsa.com
 */
public enum ITErrorCode implements ErrorCode {
    FAILED_TO_INSTANTIATE_TEST_RULE,
    TEST_PLUGINS_MISMATCH,
    EXCEPTION_OCCURRED_BEFORE_KERNEL,
    EXCEPTION_OCCURRED_AFTER_KERNEL,
    UNEXPECTED_EXCEPTION_OCCURRED,
    EXPECTED_EXCEPTION_DID_NOT_OCCURRED,
    ANOTHER_EXCEPTION_THAN_EXPECTED_OCCURRED,
    FAILED_TO_STOP_KERNEL,
    FAILED_TO_INSTANTIATE_TEST_CLASS, FAILED_TO_INITIALIZE_KERNEL
}
