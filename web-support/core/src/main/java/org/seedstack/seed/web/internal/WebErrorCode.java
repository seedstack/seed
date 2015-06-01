/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web.internal;

import org.seedstack.seed.core.api.ErrorCode;

/**
 * Enumerates all Web error codes.
 *
 * @author adrien.lauer@mpsa.com
 */
public enum WebErrorCode implements ErrorCode {
    UNEXPECTED_WEB_EXCEPTION,
    ERROR_RETRIEVING_RESOURCE,
    PLUGIN_NOT_FOUND, UNABLE_TO_DETERMINE_RESOURCE_INFO
}
