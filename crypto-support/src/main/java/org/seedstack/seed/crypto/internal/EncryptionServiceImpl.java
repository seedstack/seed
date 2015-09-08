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

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.security.cert.X509Certificate;
import java.security.*;

/**
 * Asymmetric crypting. It's used to encrypt and decrypt a text. Encrypt uses a {@link X509Certificate}. Decrypt uses the private key stored in a
 * keystore.
 * 
 * @author thierry.bouvet@mpsa.com
 */
class EncryptionServiceImpl implements EncryptionService {

    private KeyStore keyStore;

    private KeyDefinition keyDefinition;

    public EncryptionServiceImpl(KeyStore ks, KeyDefinition keyDefinition) {
        this.keyStore = ks;
        this.keyDefinition = keyDefinition;
    }

    @Override
    public byte[] encrypt(byte[] toCrypt) throws InvalidKeyException {
        PublicKey pk = this.keyDefinition.getCertificate().getPublicKey();
        return crypt(toCrypt, pk, Cipher.ENCRYPT_MODE);
    }

    @Override
    public byte[] decrypt(byte[] toDecrypt) throws InvalidKeyException {
        if (this.keyStore == null) {
            throw new IllegalArgumentException("No keystore configured so decrypt is not possible.");
        }
        Key key;
        try {
            key = keyStore.getKey(this.keyDefinition.getAlias(), this.keyDefinition.getPassword().toCharArray());
        } catch (UnrecoverableKeyException e) {
            throw new IllegalArgumentException(e);
        } catch (KeyStoreException e) {
            throw new IllegalArgumentException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(e);
        }

        return crypt(toDecrypt, key, Cipher.DECRYPT_MODE);
    }

    /**
     * Encrypt or decrypt a byte[]
     * 
     * @param crypt byte[] to encrypt or decrypt
     * @param key keystore to use
     * @param mode {@link Cipher#DECRYPT_MODE} to decrypt or {@link Cipher#ENCRYPT_MODE} to encrypt
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
