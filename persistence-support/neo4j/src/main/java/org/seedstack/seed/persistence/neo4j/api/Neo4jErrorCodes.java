/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.persistence.neo4j.api;

import org.seedstack.seed.core.api.ErrorCode;

public enum Neo4jErrorCodes implements ErrorCode {
    UNABLE_TO_LOAD_EXCEPTION_HANDLER_CLASS,
    UNKNOWN_DATABASE_TYPE,
    INVALID_PROPERTIES_URL,
    INVALID_DATABASE_SETTING,
    ACCESSING_DATABASE_OUTSIDE_TRANSACTION
}
