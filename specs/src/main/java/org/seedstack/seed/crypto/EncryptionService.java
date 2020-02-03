/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.crypto;

/**
 * Service to do asymmetric encryption and decryption. Encryption uses a {@link javax.security.cert.X509Certificate},
 * whereas decrypt uses
 * the private key stored in a key store.
 */
public interface EncryptionService {
    /**
     * Encrypt a byte[] by using a {@link javax.security.cert.X509Certificate}
     *
     * @param toEncrypt byte[] to encrypt
     * @return byte[] encrypted
     */
    byte[] encrypt(byte[] toEncrypt);

    /**
     * @param toDecrypt byte[] to decrypt
     * @return byte[] decrypted
     */
    byte[] decrypt(byte[] toDecrypt);

}
