/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.crypto;

import com.google.common.base.Strings;
import com.google.common.io.BaseEncoding;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import org.seedstack.coffig.Coffig;
import org.seedstack.coffig.spi.ConfigFunction;
import org.seedstack.coffig.spi.ConfigFunctionHolder;
import org.seedstack.coffig.spi.ConfigurationComponent;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.crypto.CryptoConfig;
import org.seedstack.seed.crypto.EncryptionService;

public class DecryptFunction implements ConfigFunctionHolder {
    private EncryptionServiceFactory encryptionServiceFactory;
    private CryptoConfig.KeyStoreConfig masterKeyStoreConfig;
    private Exception storedException;

    @Override
    public void initialize(Coffig coffig) {
        CryptoConfig cryptoConfig = coffig.get(CryptoConfig.class);
        masterKeyStoreConfig = cryptoConfig.masterKeyStore();
        if (masterKeyStoreConfig != null) {
            try {
                KeyStore keyStore = new KeyStoreLoader().load(CryptoConfig.MASTER_KEY_STORE_NAME, masterKeyStoreConfig);
                encryptionServiceFactory = new EncryptionServiceFactory(cryptoConfig, keyStore);
            } catch (Exception e) {
                storedException = e;
            }
        }
    }

    @Override
    public ConfigurationComponent fork() {
        return new DecryptFunction();
    }

    @ConfigFunction
    String decrypt(String alias, String value) {
        if (encryptionServiceFactory == null) {
            if (storedException != null) {
                throw SeedException.wrap(storedException, CryptoErrorCode.MISSING_MASTER_KEYSTORE);
            } else {
                throw SeedException.createNew(CryptoErrorCode.MISSING_MASTER_KEYSTORE);
            }
        }
        CryptoConfig.KeyStoreConfig.AliasConfig aliasConfig = masterKeyStoreConfig.getAliases().get(alias);
        if (aliasConfig == null || Strings.isNullOrEmpty(aliasConfig.getPassword())) {
            throw SeedException.createNew(CryptoErrorCode.MISSING_MASTER_KEY_PASSWORD);
        }
        EncryptionService encryptionService = encryptionServiceFactory.create(alias,
                aliasConfig.getPassword().toCharArray());
        return new String(encryptionService.decrypt(BaseEncoding.base16().decode(value)), StandardCharsets.UTF_8);
    }
}