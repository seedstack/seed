/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.crypto.internal;

import org.seedstack.seed.core.api.SeedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
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
public class KeyStoreLoader {

    private static Logger LOGGER = LoggerFactory.getLogger(KeyStoreLoader.class);

    KeyStore load(KeyStoreConfig ksConfig) {
        String name = ksConfig.getName();
        String path = ksConfig.getPath();
        String password = ksConfig.getPassword();
        String type = ksConfig.getType();
        String provider = ksConfig.getProvider();

        return load(name, path, password, type, provider);
    }

    /**
     * Loads a KeyStore from a file.
     *
     * @param name     the KeyStore name (used for logging purpose)
     * @param path     the file path
     * @param password the KeyStore password
     * @param type     the type (optional)
     * @param provider the provider (optional)
     * @return the KeyStore
     * @throws org.seedstack.seed.core.api.SeedException if anything goes wrong
     */
    KeyStore load(String name, String path, String password, @Nullable String type, @Nullable String provider) {
        if (path == null || "".equals(name) || password == null || "".equals(password)) {
            throw SeedException.createNew(CryptoErrorCodes.KEYSTORE_CONFIGURATION_ERROR)
                    .put("keyName", name)
                    .put("path", path)
                    .put("password", password);
        }

        InputStream inputStream = getInputStream(name, path);

        return loadFromInputStream(inputStream, name, password.toCharArray(), type, provider);
    }

    private InputStream getInputStream(String name, String path) {
        FileInputStream inputStream;
        try {
            inputStream = new FileInputStream(path);
        } catch (FileNotFoundException e) {
            throw SeedException.wrap(e, CryptoErrorCodes.KEYSTORE_NOT_FOUND)
                    .put("name", name).put("path", path);
        }
        return inputStream;
    }

    private KeyStore loadFromInputStream(InputStream inputStream, String name, char[] password, String type, String provider) {
        KeyStore ks;
        try {
            if (type == null || "".equals(type)) {
                type = KeyStore.getDefaultType();
            }
            if (provider == null || "".equals(provider)) {
                ks = KeyStore.getInstance(type);
            } else {
                ks = KeyStore.getInstance(type, provider);
            }

            ks.load(inputStream, password);

        } catch (KeyStoreException e) {
            throw SeedException.wrap(e, CryptoErrorCodes.KEYSTORE_TYPE_UNAVAILABLE)
                    .put("ksName", name).put("type", type);
        } catch (NoSuchProviderException e) {
            throw SeedException.wrap(e, CryptoErrorCodes.NO_KEYSTORE_PROVIDER)
                    .put("ksName", name).put("type", type);
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
