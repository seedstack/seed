/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.internal.command;

import org.seedstack.shed.exception.ErrorCode;

/**
 * SeedStack commands error codes.
 */
enum CommandErrorCode implements ErrorCode {
    ARGUMENT_INDEX_COLLISION,
    COMMAND_DEFINITION_NOT_FOUND,
    MISSING_ARGUMENTS,
    MISSING_MANDATORY_OPTION,
    TOO_MANY_ARGUMENTS,
    UNABLE_TO_INJECT_ARGUMENT,
    UNABLE_TO_INJECT_OPTION,
    UNABLE_TO_INSTANTIATE_COMMAND
}
