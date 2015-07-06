/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.jms.spi;

import org.seedstack.seed.core.api.ErrorCode;

/**
 * JMS error codes.
 *
 * @author pierre.thirouin@ext.mpsa.com
 */
public enum JmsErrorCodes implements ErrorCode {
    UNEXPECTED_EXCEPTION,
    INITIALIZATION_EXCEPTION,
    PLUGIN_NOT_FOUND,
    UNABLE_TO_START_JMS_CONNECTION,
    UNABLE_TO_CREATE_CONNECTION_FACTORY,
    MISCONFIGURED_CONNECTION_FACTORY,
    UNRECOGNIZED_CONNECTION_FACTORY,
    MISSING_CONNECTION_FACTORY,
    UNABLE_TO_CREATE_JMS_CONNECTION,
    UNABLE_TO_LOAD_CLASS,
    UNABLE_TO_CREATE_SESSION,
    UNKNOWN_DESTINATION_TYPE,
    UNABLE_TO_CREATE_DESTINATION,
    NO_JNDI_CONTEXT,
    MISSING_JNDI_CONTEXT,
    JNDI_LOOKUP_ERROR,
    UNABLE_TO_CREATE_MESSAGE_CONSUMER,
    DUPLICATE_MESSAGE_LISTENER_DEFINITION_NAME,
    MESSAGE_POLLER_REQUIRED_IN_JEE_MODE,
    UNABLE_TO_CREATE_POLLER, CANNOT_SET_CLIENT_ID_IN_JEE_MODE, DUPLICATE_CONNECTION_NAME
}
