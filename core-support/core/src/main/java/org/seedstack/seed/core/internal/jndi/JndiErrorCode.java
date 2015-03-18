/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.jndi;

import org.seedstack.seed.core.api.ErrorCode;

/**
 * JNDI error codes.
 *
 * @author adrien.lauer@mpsa.com
 */
enum JndiErrorCode implements ErrorCode {
    UNABLE_TO_CONFIGURE_DEFAULT_JNDI_CONTEXT,
    UNABLE_TO_CONFIGURE_ADDITIONAL_JNDI_CONTEXT,
    MISSING_JNDI_PROPERTIES,
    UNABLE_TO_REGISTER_INJECTION_FOR_RESOURCE
}
