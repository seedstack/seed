/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.assertions;

import org.seedstack.seed.core.api.ErrorCode;

/**
 * SEED core assertions error codes.
 *
 * @author epo.jemba@ext.mpsa.com
 * @author pierre.thirouin@ext.mpsa.com
 */
enum CoreAssertionsErrorCode implements ErrorCode {
    UNEXPECTED_EXCEPTION,
	CLASS_IS_NOT_INJECTABLE,
	CLASS_IS_NOT_INJECTED_WITH,
	BAD_PROPERTY_VALUE,
	OBJECT_IS_NULL,
	METHOD_DOES_NOT_EXIST
}
