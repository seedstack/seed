/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.crypto.internal;

import org.seedstack.seed.ErrorCode;

/**
 * Cryptography error codes.
 *
 * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
 */
public enum  CryptoErrorCodes implements ErrorCode {
    ALGORITHM_CANNOT_BE_FOUND,
    CERTIFICATE_NOT_FOUND,
    INVALID_CLIENT_AUTHENTICATION_MODE,
    INVALID_QUALIFIER_ANNOTATION,
    KEYSTORE_CONFIGURATION_ERROR,
    KEYSTORE_NOT_FOUND,
    KEYSTORE_TYPE_UNAVAILABLE,
    MISSING_ALIAS_PASSWORD,
    MISSING_MASTER_KEYSTORE,
    MISSING_MASTER_KEY_PASSWORD,
    MISSING_PUBLIC_KEY,
    MISSING_PRIVATE_KEY,
    MISSING_SSL_KEY_STORE_CONFIGURATION,
    MISSING_SSL_TRUST_STORE_CONFIGURATION,
    NO_KEYSTORE_CONFIGURED,
    NO_KEYSTORE_PROVIDER,
    UNRECOVERABLE_KEY,

    // wrapped exception error codes (don't have message/fix)
    ENABLE_TO_GET_CIPHER,
    ENABLE_TO_LOAD_CERTIFICATE,
    ENABLE_TO_READ_CERTIFICATE,
    INCORRECT_PASSWORD,
    UNEXPECTED_EXCEPTION,
}
