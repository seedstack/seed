/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.jms.internal;

import org.seedstack.seed.core.api.ErrorCode;

/**
 * JMS error codes.
 *
 * @author Pierre Thirouin <pierre.thirouin@ext.mpsa.com>
 *         03/11/2014
 */
public enum SeedJmsErrorCodes implements ErrorCode {
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
    MISSING_CONNECTION_FOR_MESSAGE_LISTENER,
    UNABLE_TO_CREATE_SESSION_FOR_LISTENER,
    UNKNOWN_DESTINATION_TYPE,
    UNABLE_TO_CREATE_DESTINATION_FOR_LISTENER,
    NO_JNDI_CONTEXT,
    MISSING_JNDI_CONTEXT,
    JNDI_LOOKUP_ERROR,
    DUPLICATE_MESSAGE_LISTENER_DEFINITION_NAME, DUPLICATE_CONNECTION_NAME
}
