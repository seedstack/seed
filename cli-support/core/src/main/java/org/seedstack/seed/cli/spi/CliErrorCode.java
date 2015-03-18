/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.cli.spi;

import org.seedstack.seed.core.api.ErrorCode;

/**
 * Enumerates all error codes for command line support.
 *
 * @author adrien.lauer@mpsa.com
 */
public enum CliErrorCode implements ErrorCode {
    NO_COMMAND_LINE_HANDLER_FOUND,
    UNEXPECTED_CLI_ERROR
}
