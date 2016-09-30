/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.cli;

import org.seedstack.shed.exception.ErrorCode;

enum CliErrorCode implements ErrorCode {
    COMMAND_LINE_HANDLER_NOT_FOUND,
    EXCEPTION_OCCURRED_BEFORE_CLI_TEST,
    NO_COMMAND_SPECIFIED,
    ERROR_PARSING_COMMAND_LINE,
    UNABLE_TO_INJECT_OPTION,
    MISSING_ARGUMENTS,
    UNABLE_TO_INJECT_ARGUMENTS,
    UNSUPPORTED_OPTION_FIELD_TYPE,
    ODD_NUMBER_OF_OPTION_ARGUMENTS,
    WRONG_NUMBER_OF_OPTION_ARGUMENTS,
    CONFLICTING_COMMAND_ANNOTATIONS, UNEXPECTED_EXCEPTION
}
