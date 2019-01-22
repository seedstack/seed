/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.crypto;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.crypto.EncryptionService;

class EncryptionServiceImpl implements EncryptionService {
    private static final String CIPHER = "RSA/ECB/PKCS1PADDING";
    private final String alias;
    private final PublicKey publicKey;
    private final Key privateKey;

    EncryptionServiceImpl(String alias, PublicKey publicKey, Key privateKey) {
        this.alias = alias;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    @Override
    public byte[] encrypt(byte[] toCrypt) {
        if (this.publicKey == null) {
            throw SeedException.createNew(CryptoErrorCode.MISSING_PUBLIC_KEY).put("alias", alias);
        }
        return doEncryptionOrDecryption(toCrypt, publicKey, Cipher.ENCRYPT_MODE);
    }

    @Override
    public byte[] decrypt(byte[] toDecrypt) {
        if (this.privateKey == null) {
            throw SeedException.createNew(CryptoErrorCode.MISSING_PRIVATE_KEY).put("alias", alias);
        }
        return doEncryptionOrDecryption(toDecrypt, privateKey, Cipher.DECRYPT_MODE);
    }

    /**
     * Encrypts or decrypts a byte[].
     *
     * @param crypt byte[] to encrypt or decrypt
     * @param key   key to use
     * @param mode  {@link Cipher#DECRYPT_MODE} to decrypt or {@link Cipher#ENCRYPT_MODE} to encrypt
     * @return byte[] encrypted or decrypted
     */
    private byte[] doEncryptionOrDecryption(byte[] crypt, Key key, int mode) {
        Cipher rsaCipher;
        try {
            rsaCipher = Cipher.getInstance(CIPHER);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw SeedException.wrap(e, CryptoErrorCode.UNABLE_TO_GET_CIPHER)
                    .put("alias", alias)
                    .put("cipher", CIPHER);
        }
        try {
            rsaCipher.init(mode, key);
        } catch (InvalidKeyException e) {
            throw SeedException.wrap(e, CryptoErrorCode.INVALID_KEY)
                    .put("alias", alias);
        }
        try {
            return rsaCipher.doFinal(crypt);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw SeedException.wrap(e, CryptoErrorCode.UNEXPECTED_EXCEPTION);
        }
    }
}
