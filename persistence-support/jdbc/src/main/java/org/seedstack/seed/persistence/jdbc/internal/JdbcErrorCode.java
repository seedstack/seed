/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
/*
 * Creation : 17 févr. 2015
 */
package org.seedstack.seed.persistence.jdbc.internal;

import org.seedstack.seed.core.api.ErrorCode;

/**
 * JDBC Error codes
 */
public enum JdbcErrorCode implements ErrorCode {

    ACCESSING_JDBC_CONNECTION_OUTSIDE_TRANSACTION,

    CANNOT_CONNECT_TO_JDBC_DATASOURCE,

    JDBC_COMMIT_EXCEPTION,

    JDBC_ROLLBACK_EXCEPTION,

    JDBC_CLOSE_EXCEPTION,

    WRONG_JDBC_DRIVER,

    WRONG_DATASOURCE_PROVIDER,

    WRONG_DATASOURCE_CONTEXT;

}
