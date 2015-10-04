/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
/*
 * Creation : 12 mars 2015
 */
package org.seedstack.seed.crypto.internal;

import org.seedstack.seed.crypto.api.EncryptionService;

import javax.annotation.Nullable;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.security.cert.X509Certificate;
import java.security.*;

/**
 * Asymmetric crypting. It's used to encrypt and decrypt a data. Encrypt uses a {@link X509Certificate}. Decrypt uses the private key stored in a
 * KeyStore.
 *
 * @author thierry.bouvet@mpsa.com
 */
class EncryptionServiceImpl implements EncryptionService {

    private final String alias;
    private final PublicKey publicKey;
    private final Key privateKey;

    /**
     * Constructs an encryption service for a given key pair.
     *
     * @param publicKey  the public key used to encrypt
     * @param privateKey the private key used to decrypt
     */
    public EncryptionServiceImpl(String alias, @Nullable PublicKey publicKey, @Nullable Key privateKey) {
        this.alias = alias;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    @Override
    public byte[] encrypt(byte[] toCrypt) throws InvalidKeyException {
        if (this.publicKey == null) {
            throw new IllegalArgumentException("No public key for the alias " + alias + " is defined so encrypt is impossible.");
        }
        return crypt(toCrypt, publicKey, Cipher.ENCRYPT_MODE);
    }

    @Override
    public byte[] decrypt(byte[] toDecrypt) throws InvalidKeyException {
        if (this.privateKey == null) {
            throw new IllegalArgumentException("No private key for the alias " + alias + " is defined so decrypt is impossible.");
        }
        return crypt(toDecrypt, privateKey, Cipher.DECRYPT_MODE);
    }

    /**
     * Encrypt or decrypt a byte[]
     *
     * @param crypt byte[] to encrypt or decrypt
     * @param key   key to use
     * @param mode  {@link Cipher#DECRYPT_MODE} to decrypt or {@link Cipher#ENCRYPT_MODE} to encrypt
     * @return byte[] encrypted or decrypted
     * @throws InvalidKeyException if the given key is inappropriate for initializing this cipher. See {@link Cipher#init(int, java.security.Key)}
     */
    private byte[] crypt(byte[] crypt, Key key, int mode) throws InvalidKeyException {
        Cipher rsaCipher;
        try {
            rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1PADDING");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(e);
        } catch (NoSuchPaddingException e) {
            throw new IllegalArgumentException(e);
        }
        rsaCipher.init(mode, key);

        try {
            return rsaCipher.doFinal(crypt);
        } catch (IllegalBlockSizeException e) {
            throw new IllegalArgumentException(e);
        } catch (BadPaddingException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
