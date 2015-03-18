/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.validation.api;


import org.seedstack.seed.core.api.ErrorCode;
import org.seedstack.seed.core.api.SeedException;

/**
 * Exception class for validation errors.
 *
 * @author epo.jemba@ext.mpsa.com
 */
public class ValidationException extends SeedException {
    private static final long serialVersionUID = 1L;

	protected ValidationException(ErrorCode errorCode) {
		super(errorCode);
	}
}
