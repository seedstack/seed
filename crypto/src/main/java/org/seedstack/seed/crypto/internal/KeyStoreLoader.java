/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.crypto.internal;

import org.seedstack.seed.SeedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;

/**
 * This class allows to load a KeyStore from a file. It supports any type of KeyStore or provider.
 *
 * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
 */
class KeyStoreLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(KeyStoreLoader.class);

    KeyStore load(KeyStoreConfig ksConfig) {
        String name = ksConfig.getName();
        String path = ksConfig.getPath();
        return loadFromInputStream(getInputStream(name, path), ksConfig);
    }

    private InputStream getInputStream(String name, String path) {
        FileInputStream inputStream;
        try {
            inputStream = new FileInputStream(path);
        } catch (FileNotFoundException e) {
            throw SeedException.wrap(e, CryptoErrorCodes.KEYSTORE_NOT_FOUND).put("name", name).put("path", path);
        }
        return inputStream;
    }

    private KeyStore loadFromInputStream(InputStream inputStream, KeyStoreConfig ksConfig) {
        KeyStore ks;
        String type = ksConfig.getType();
        String provider = ksConfig.getProvider();
        try {
            if (type == null || "".equals(type)) {
                type = KeyStore.getDefaultType();
            }
            if (provider == null || "".equals(provider)) {
                ks = KeyStore.getInstance(type);
            } else {
                ks = KeyStore.getInstance(type, provider);
            }

            ks.load(inputStream, ksConfig.getPassword().toCharArray());

        } catch (KeyStoreException e) {
            throw SeedException.wrap(e, CryptoErrorCodes.KEYSTORE_TYPE_UNAVAILABLE)
                    .put("ksName", ksConfig.getName()).put("type", type);
        } catch (NoSuchProviderException e) {
            throw SeedException.wrap(e, CryptoErrorCodes.NO_KEYSTORE_PROVIDER)
                    .put("ksName", ksConfig.getName()).put("provider", provider);
        } catch (NoSuchAlgorithmException e) {
            throw SeedException.wrap(e, CryptoErrorCodes.ALGORITHM_CANNOT_BE_FOUND);
        } catch (CertificateException e) {
            throw SeedException.wrap(e, CryptoErrorCodes.ENABLE_TO_LOAD_CERTIFICATE);
        } catch (IOException e) {
            throw SeedException.wrap(e, CryptoErrorCodes.INCORRECT_PASSWORD);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        return ks;
    }
}
