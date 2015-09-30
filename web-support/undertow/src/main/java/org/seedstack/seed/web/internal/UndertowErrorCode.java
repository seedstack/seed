/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web.internal;

import org.seedstack.seed.core.api.ErrorCode;

/**
 * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
 */
public enum UndertowErrorCode implements ErrorCode {
    UNEXPECTED_EXCEPTION,
    MISSING_SSL_CONFIGURATION,
    ILLEGAL_HOST_CONFIGURATION,
    ILLEGAL_PORT_CONFIGURATION
}
