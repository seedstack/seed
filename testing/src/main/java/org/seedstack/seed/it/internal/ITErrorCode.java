/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.it.internal;

import org.seedstack.shed.exception.ErrorCode;

public enum ITErrorCode implements ErrorCode {
    ANOTHER_EXCEPTION_THAN_EXPECTED_OCCURRED,
    EXCEPTION_OCCURRED_AFTER_KERNEL,
    EXCEPTION_OCCURRED_BEFORE_KERNEL,
    EXPECTED_EXCEPTION_DID_NOT_OCCURRED,
    FAILED_TO_INITIALIZE_KERNEL,
    FAILED_TO_INSTANTIATE_TEST_RULE,
    FAILED_TO_STOP_KERNEL,
    TEST_PLUGINS_MISMATCH,
    UNABLE_TO_INSTANTIATE_IT_MODULE,
    UNEXPECTED_EXCEPTION_OCCURRED
}
