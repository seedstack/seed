/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.jersey2.internal;

import org.seedstack.shed.exception.ErrorCode;

enum Jersey2ErrorCode implements ErrorCode {
    MISSING_INJECTOR,
    UNSUPPORTED_JERSEY_DEPENDENCY_INJECTION, MISSING_SERVLET_CONTEXT
}
