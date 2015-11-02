/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.crypto.internal;

import org.apache.commons.configuration.Configuration;
import org.seedstack.seed.core.api.SeedException;
import org.seedstack.seed.core.utils.SeedReflectionUtils;

import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import static org.seedstack.seed.core.utils.ConfigurationUtils.buildKey;
import static org.seedstack.seed.crypto.internal.CryptoPlugin.*;

/**
 * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
 */
public class KeyPairConfigFactory {

    private final Configuration cryptoConfiguration;

    public KeyPairConfigFactory(Configuration cryptoConfiguration) {
        this.cryptoConfiguration = cryptoConfiguration;
    }

    /**
     * Creates a key pair configuration.
     *
     * @param keyStore the keystore containing the key pair
     */
    public List<KeyPairConfig> create(String keyStoreName, KeyStore keyStore) {
        List<KeyPairConfig> keyPairConfigs = new ArrayList<KeyPairConfig>();
        try {
            Enumeration<String> aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                KeyPairConfig keyPairConfig = createKeyPairFromAlias(aliases.nextElement(), keyStoreName);
                keyPairConfigs.add(keyPairConfig);
            }
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
        return keyPairConfigs;
    }

    private KeyPairConfig createKeyPairFromAlias(String alias, String keyStoreName) {
        String aliasPassword = getPassword(alias, keyStoreName);
        String location = getLocation(alias);
        String qualifier = getQualifier(alias, keyStoreName);
        return new KeyPairConfig(keyStoreName, alias, aliasPassword, location, qualifier);
    }

    private String getQualifier(String alias, String keyStoreName) {
        return this.cryptoConfiguration.getString(buildKey(KEYSTORE, keyStoreName, ALIAS, alias, QUALIFIER));
    }

    private String getLocation(String alias) {
        String certLocation;

        // Find the certificate location from the classpath
        String certResource = cryptoConfiguration.getString(buildKey(CERT, alias, CERT_RESOURCE));
        if (certResource != null) {
            URL urlResource = SeedReflectionUtils.findMostCompleteClassLoader().getResource(certResource);
            if (urlResource == null) {
                throw SeedException.createNew(CryptoErrorCodes.CERTIFICATE_NOT_FOUND)
                        .put("alias", alias).put("certResource", certResource);
            }
            certLocation = urlResource.getFile();
        } else {
            // Otherwise get the file path from the configuration
            certLocation = cryptoConfiguration.getString(buildKey(CERT, alias, CERT_FILE));
        }
        return certLocation;
    }

    private String getPassword(String alias, String keyStoreName) {
        return this.cryptoConfiguration.getString(buildKey(KEYSTORE, keyStoreName, ALIAS, alias, PASSWORD));
    }
}
