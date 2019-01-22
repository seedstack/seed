/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.crypto;

import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.crypto.CryptoConfig;
import org.seedstack.shed.ClassLoaders;

class KeyPairConfigFactory {

    private final CryptoConfig cryptoConfig;

    KeyPairConfigFactory(CryptoConfig cryptoConfig) {
        this.cryptoConfig = cryptoConfig;
    }

    List<KeyPairConfig> create(String keyStoreName, KeyStore keyStore) {
        List<KeyPairConfig> keyPairConfigs = new ArrayList<>();
        try {
            Enumeration<String> aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                KeyPairConfig keyPairConfig = createKeyPairFromAlias(aliases.nextElement(), keyStoreName);
                keyPairConfigs.add(keyPairConfig);
            }
        } catch (KeyStoreException e) {
            throw SeedException.wrap(e, CryptoErrorCode.UNEXPECTED_EXCEPTION);
        }
        return keyPairConfigs;
    }

    private KeyPairConfig createKeyPairFromAlias(String alias, String keyStoreName) {
        String aliasPassword = getPassword(alias, keyStoreName);
        String location = getLocation(alias);
        String qualifier = getQualifier(alias, keyStoreName);
        return new KeyPairConfig(keyStoreName, alias, aliasPassword, location, qualifier);
    }

    private String getLocation(String certificateName) {
        CryptoConfig.CertificateConfig certificateConfig = cryptoConfig.certificates().get(certificateName);

        if (certificateConfig != null) {
            // Find the certificate location from the classpath
            String certResource = certificateConfig.getResource();
            if (certResource != null) {
                URL urlResource = ClassLoaders.findMostCompleteClassLoader(KeyPairConfigFactory.class)
                        .getResource(certResource);
                if (urlResource == null) {
                    throw SeedException.createNew(CryptoErrorCode.CERTIFICATE_NOT_FOUND)
                            .put("certificateName", certificateName).put("certResource", certResource);
                }
                return urlResource.getFile();
            } else {
                // Otherwise get the file path from the configuration
                return certificateConfig.getFile();
            }
        } else {
            return null;
        }
    }

    private String getPassword(String alias, String keyStoreName) {
        CryptoConfig.KeyStoreConfig.AliasConfig aliasConfig = getAliasConfig(alias, keyStoreName);
        if (aliasConfig != null) {
            return aliasConfig.getPassword();
        } else {
            return null;
        }
    }

    private String getQualifier(String alias, String keyStoreName) {
        CryptoConfig.KeyStoreConfig.AliasConfig aliasConfig = getAliasConfig(alias, keyStoreName);
        if (aliasConfig != null) {
            return aliasConfig.getQualifier();
        } else {
            return null;
        }
    }

    private CryptoConfig.KeyStoreConfig.AliasConfig getAliasConfig(String alias, String keyStoreName) {
        CryptoConfig.KeyStoreConfig keyStoreConfig = cryptoConfig.keyStores().get(keyStoreName);
        if (keyStoreConfig != null) {
            return keyStoreConfig.getAliases().get(alias);
        } else {
            return null;
        }
    }
}
