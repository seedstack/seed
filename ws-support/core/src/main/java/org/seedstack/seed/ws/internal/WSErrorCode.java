/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.ws.internal;

import org.seedstack.seed.core.api.ErrorCode;

enum WSErrorCode implements ErrorCode {
    ENDPOINT_URL_MISSING,
    IMPLEMENTATION_CLASS_MISSING,
    UNABLE_TO_LOAD_IMPLEMENTATION_CLASS,
    WSDL_LOCATION_MISSING,
    UNABLE_TO_FIND_WSDL,
    NO_WS_CONFIGURATION,
    UNABLE_TO_LOAD_REALM_AUTHENTICATION_ADAPTER_CLASS, INVALID_REALM_AUTHENTICATION_ADAPTER_CLASS, MALFORMED_ENDPOINT_URL
}
