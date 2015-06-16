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

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.security.cert.X509Certificate;

import org.seedstack.seed.crypto.api.EncryptionService;

/**
 * Asymetric crypting. It's used to encrypt and decrypt a text. Encrypt uses a {@link X509Certificate}. Decrypt uses the provate key stored in a
 * keystore.
 * 
 * @author thierry.bouvet@mpsa.com
 */
class EncryptionServiceImpl implements EncryptionService {

    private KeyStore keyStore;

    private CertificateDefinition certificateDefintion;

    public EncryptionServiceImpl(KeyStore ks, CertificateDefinition certificateDefintion) {
        this.keyStore = ks;
        this.certificateDefintion = certificateDefintion;
    }

    @Override
    public byte[] encrypt(byte[] toCrypt) throws InvalidKeyException {
        PublicKey pk = this.certificateDefintion.getCertificate().getPublicKey();
        return crypt(toCrypt, pk, Cipher.ENCRYPT_MODE);
    }

    @Override
    public byte[] decrypt(byte[] toDecrypt) throws InvalidKeyException {
        if (this.keyStore == null) {
            throw new IllegalArgumentException("No keystore configured so decrypt is not possible.");
        }
        Key key;
        try {
            key = keyStore.getKey(this.certificateDefintion.getAlias(), this.certificateDefintion.getPassword().toCharArray());
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
        Cipher rsaCipher = null;
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
