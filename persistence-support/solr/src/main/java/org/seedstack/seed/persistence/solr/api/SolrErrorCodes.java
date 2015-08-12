/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.persistence.solr.api;

import org.seedstack.seed.core.api.ErrorCode;

/**
 * Error codes for Solr support exceptions.
 */
public enum SolrErrorCodes implements ErrorCode {
    UNSUPPORTED_CLIENT_TYPE,
    MISSING_URL_CONFIGURATION,
    UNABLE_TO_CREATE_CLIENT,
    UNABLE_TO_COMMIT,
    ACCESSING_SOLR_CLIENT_OUTSIDE_TRANSACTION, UNABLE_TO_ROLLBACK
}
