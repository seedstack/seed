/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.transaction;

import org.seedstack.shed.exception.ErrorCode;

enum TransactionErrorCode implements ErrorCode {
    NO_TRANSACTION_ALLOWED_WHEN_USING_PROPAGATION_NEVER,
    NO_TRANSACTION_HANDLER_SPECIFIED,
    PROPAGATION_NOT_SUPPORTED,
    SPECIFIED_TRANSACTION_HANDLER_NOT_FOUND,
    TRANSACTION_NEEDED_WHEN_USING_PROPAGATION_MANDATORY,
    TRANSACTION_PROPAGATION_ERROR,
    TRANSACTION_SUSPENSION_IS_NOT_SUPPORTED,
    UNABLE_TO_BEGIN_JTA_TRANSACTION,
    UNABLE_TO_COMMIT_JTA_TRANSACTION,
    UNABLE_TO_CREATE_TRANSACTIONAL_PROXY,
    UNABLE_TO_FIND_JTA_TRANSACTION,
    UNABLE_TO_FIND_JTA_TRANSACTION_MANAGER,
    UNABLE_TO_ROLLBACK_JTA_TRANSACTION
}
