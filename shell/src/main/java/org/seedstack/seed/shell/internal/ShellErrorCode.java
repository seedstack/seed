/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.shell.internal;

import org.seedstack.seed.core.api.ErrorCode;

enum ShellErrorCode implements ErrorCode {
    MODE_SYNTAX_ERROR,
    ILLEGAL_MODE,
    ILLEGAL_MODE_OPTION,
    COMMAND_PARSING_ERROR,
    COMMAND_SYNTAX_ERROR,
    COMMAND_PREPARATION_ERROR,
    OPTIONS_SYNTAX_ERROR,
    MISSING_COMMAND
}
