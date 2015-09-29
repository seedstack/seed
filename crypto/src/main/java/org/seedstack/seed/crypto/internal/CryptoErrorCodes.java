/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.crypto.internal;

import org.seedstack.seed.core.api.ErrorCode;

/**
 * Cryptography error codes.
 *
 * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
 */
enum  CryptoErrorCodes implements ErrorCode {
    UNEXPECTED_EXCEPTION,
    ENABLE_TO_GENERATE_SSL_CERTIFICATE,
    ALGORITHM_CANNOT_BE_FOUND,
    ENABLE_TO_LOAD_CERTIFICATE,
    INCORRECT_PASSWORD,
    CANNOT_CLOSE_KEYSTORE,
    KEYSTORE_NOT_FOUND,
    UNRECOVERABLE_KEY,
    UNABLE_TO_GENERATE_SELF_CERTIFICATE,
    NO_KEYSTORE_PROVIDER,
    KEYSTORE_TYPE_UNAVAILABLE, KEYSTORE_CONFIGURATION_ERROR
}
