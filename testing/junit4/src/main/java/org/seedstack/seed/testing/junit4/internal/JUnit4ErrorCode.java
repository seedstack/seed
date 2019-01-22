/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.testing.junit4.internal;

import org.seedstack.shed.exception.ErrorCode;

enum JUnit4ErrorCode implements ErrorCode {
    ANOTHER_EXCEPTION_THAN_EXPECTED_OCCURRED,
    CONFLICTING_ARGUMENTS,
    CONFLICTING_EXPECTED_EXCEPTIONS,
    CONFLICTING_LAUNCH_MODES,
    CONFLICTING_LAUNCHERS,
    EXPECTED_EXCEPTION_DID_NOT_OCCURRED,
    FAILED_TO_LAUNCH_APPLICATION,
    FAILED_TO_SHUTDOWN_APPLICATION,
    MISSING_LAUNCHER_FOR_TEST
}
