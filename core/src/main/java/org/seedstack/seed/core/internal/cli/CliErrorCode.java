/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.internal.cli;

import org.seedstack.shed.exception.ErrorCode;

enum CliErrorCode implements ErrorCode {
    CONFLICTING_COMMAND_ANNOTATIONS,
    ERROR_PARSING_COMMAND_LINE,
    MISSING_ARGUMENTS,
    ODD_NUMBER_OF_OPTION_ARGUMENTS,
    UNABLE_TO_INJECT_ARGUMENTS,
    UNABLE_TO_INJECT_OPTION,
    UNSUPPORTED_OPTION_FIELD_TYPE,
    WRONG_NUMBER_OF_OPTION_ARGUMENTS
}
