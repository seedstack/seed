/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
/*
 * Creation : 12 mars 2015
 */
package org.seedstack.seed.crypto;

import javax.crypto.Cipher;
import javax.security.cert.X509Certificate;
import java.security.InvalidKeyException;

/**
 * Service to do asymmetric encryption and decryption. Encryption uses a {@link X509Certificate}, whereas decrypt uses
 * the private key stored in a key store.
 *
 * @author thierry.bouvet@mpsa.com
 */
public interface EncryptionService {
    /**
     * Encrypt a byte[] by using a {@link X509Certificate}
     * 
     * @param toEncrypt byte[] to encrypt
     * @return byte[] encrypted
     * @throws InvalidKeyException if the given key is inappropriate for initializing this cipher. See {@link Cipher#init(int, java.security.Key)}
     */
    byte[] encrypt(byte[] toEncrypt) throws InvalidKeyException;

    /**
     * @param toDecrypt byte[] to decrypt
     * @return byte[] decrypted
     * @throws InvalidKeyException if the given key is inappropriate for initializing this cipher. See {@link Cipher#init(int, java.security.Key)}
     */
    byte[] decrypt(byte[] toDecrypt) throws InvalidKeyException;

}
