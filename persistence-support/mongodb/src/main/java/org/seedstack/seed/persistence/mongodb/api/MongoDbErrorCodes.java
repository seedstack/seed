/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.persistence.mongodb.api;

import org.seedstack.seed.core.api.ErrorCode;

public enum MongoDbErrorCodes implements ErrorCode {
    MISSING_URI,
    UNABLE_TO_PARSE_SERVER_ADDRESS,
    UNSUPPORTED_AUTHENTICATION_MECHANISM,
    UNKNOWN_CLIENT_SPECIFIED,
    DUPLICATE_DATABASE_NAME,
    UNABLE_TO_INSTANTIATE_CLASS,
    UNKNOWN_CLIENT_OPTION,
    UNKNOWN_CLIENT_SETTING,
    MISSING_HOSTS_CONFIGURATION,
    INVALID_CREDENTIAL_SYNTAX
}
