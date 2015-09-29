/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.ldap;

import org.seedstack.seed.core.api.ErrorCode;

/**
 * Enum for Error codes in LDAP security support
 */
public enum LDAPErrorCode implements ErrorCode {
    LDAP_ERROR, NO_SUCH_ACCOUNT, INVALID_CREDENTIALS, CONNECT_ERROR, NO_HOST_DEFINED;
}
