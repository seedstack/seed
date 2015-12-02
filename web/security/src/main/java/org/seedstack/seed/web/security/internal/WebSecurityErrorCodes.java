/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web.security.internal;

import org.seedstack.seed.ErrorCode;

enum WebSecurityErrorCodes implements ErrorCode {
    UNABLE_TO_APPLY_XSRF_PROTECTION,
    MISSING_XSRF_COOKIE,
    MISSING_XSRF_HEADER,
    INVALID_XSRF_TOKEN
}
