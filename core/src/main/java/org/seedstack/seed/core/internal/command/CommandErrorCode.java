/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.command;

import org.seedstack.seed.core.api.ErrorCode;

/**
 * SEED commands error codes.
 *
 * @author adrien.lauer@mpsa.com
 */
enum CommandErrorCode implements ErrorCode {
    ARGUMENT_INDEX_COLLISION,
    COMMAND_DEFINITION_NOT_FOUND,
    UNABLE_TO_INSTANTIATE_COMMAND,
    TOO_MANY_ARGUMENTS,
    MISSING_ARGUMENTS,
    UNABLE_TO_INJECT_ARGUMENT,
    MISSING_MANDATORY_OPTION,
    UNABLE_TO_INJECT_OPTION
}
