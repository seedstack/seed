/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.cli.internal;

import org.seedstack.shed.exception.ErrorCode;

enum CliErrorCode implements ErrorCode {
    COMMAND_LINE_HANDLER_ALREADY_RUN,
    COMMAND_LINE_HANDLER_NOT_FOUND,
    NO_COMMAND_SPECIFIED,
    UNEXPECTED_STATUS_CODE
}
