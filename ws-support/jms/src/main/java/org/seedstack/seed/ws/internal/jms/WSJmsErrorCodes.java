/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.ws.internal.jms;

import org.seedstack.seed.core.api.ErrorCode;

enum WSJmsErrorCodes implements ErrorCode {
    UNABLE_TO_ROLLBACK_WS_JMS_MESSAGE,
    UNABLE_TO_REGISTER_MESSAGE_LISTENER,
    UNABLE_TO_GET_TRANSACTED_STATUS
}
