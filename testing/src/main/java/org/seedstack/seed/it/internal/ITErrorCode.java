/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.it.internal;

import org.seedstack.shed.exception.ErrorCode;

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
    FAILED_TO_INSTANTIATE_TEST_CLASS, UNABLE_TO_INSTANTIATE_IT_MODULE, FAILED_TO_INITIALIZE_KERNEL
}
