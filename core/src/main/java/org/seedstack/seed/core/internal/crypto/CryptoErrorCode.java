/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.internal.crypto;

import org.seedstack.shed.exception.ErrorCode;

/**
 * Cryptography error codes.
 */
public enum CryptoErrorCode implements ErrorCode {
    ALGORITHM_CANNOT_BE_FOUND,
    CERTIFICATE_NOT_FOUND,
    INCORRECT_PASSWORD,
    INVALID_KEY,
    INVALID_QUALIFIER_ANNOTATION,
    KEYSTORE_CONFIGURATION_ERROR,
    KEYSTORE_NOT_FOUND,
    KEYSTORE_TYPE_UNAVAILABLE,
    MISSING_ALIAS_PASSWORD,
    MISSING_MASTER_KEYSTORE,
    MISSING_MASTER_KEY_PASSWORD,
    MISSING_PRIVATE_KEY,
    MISSING_PUBLIC_KEY,
    NO_KEYSTORE_CONFIGURED,
    NO_KEYSTORE_PROVIDER,
    UNABLE_TO_GET_CIPHER,
    UNABLE_TO_LOAD_CERTIFICATE,
    UNABLE_TO_READ_CERTIFICATE,
    UNEXPECTED_EXCEPTION,
    UNRECOVERABLE_KEY
}
