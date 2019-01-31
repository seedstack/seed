/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.crypto;

import com.google.common.base.Strings;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.crypto.CryptoConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class allows to load a KeyStore from a file. It supports any type of KeyStore or provider.
 */
class KeyStoreLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(KeyStoreLoader.class);

    KeyStore load(String name, CryptoConfig.KeyStoreConfig ksConfig) {
        String path = ksConfig.getPath();
        if (Strings.isNullOrEmpty(name) || Strings.isNullOrEmpty(path) || Strings.isNullOrEmpty(
                ksConfig.getPassword())) {
            throw SeedException.createNew(CryptoErrorCode.KEYSTORE_CONFIGURATION_ERROR)
                    .put("ksName", name)
                    .put("path", path);
        }
        return loadFromInputStream(name, getInputStream(name, path), ksConfig);
    }

    private InputStream getInputStream(String name, String path) {
        FileInputStream inputStream;
        try {
            inputStream = new FileInputStream(path);
        } catch (FileNotFoundException e) {
            throw SeedException.wrap(e, CryptoErrorCode.KEYSTORE_NOT_FOUND).put("name", name).put("path", path);
        }
        return inputStream;
    }

    private KeyStore loadFromInputStream(String name, InputStream inputStream, CryptoConfig.KeyStoreConfig ksConfig) {
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
            throw SeedException.wrap(e, CryptoErrorCode.KEYSTORE_TYPE_UNAVAILABLE)
                    .put("ksName", name)
                    .put("type", type);
        } catch (NoSuchProviderException e) {
            throw SeedException.wrap(e, CryptoErrorCode.NO_KEYSTORE_PROVIDER)
                    .put("ksName", name)
                    .put("provider", provider);
        } catch (NoSuchAlgorithmException e) {
            throw SeedException.wrap(e, CryptoErrorCode.ALGORITHM_CANNOT_BE_FOUND)
                    .put("ksName", name);
        } catch (CertificateException e) {
            throw SeedException.wrap(e, CryptoErrorCode.UNABLE_TO_LOAD_CERTIFICATE)
                    .put("ksName", name);
        } catch (IOException e) {
            throw SeedException.wrap(e,
                    e.getCause() instanceof UnrecoverableKeyException ? CryptoErrorCode.INCORRECT_PASSWORD :
                            CryptoErrorCode.UNEXPECTED_EXCEPTION)
                    .put("ksName", name);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                LOGGER.warn("Unable to close key store input stream", e);
            }
        }
        return ks;
    }
}
