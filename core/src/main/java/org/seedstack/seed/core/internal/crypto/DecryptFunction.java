/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.crypto;

import com.google.common.base.Strings;
import org.seedstack.coffig.Coffig;
import org.seedstack.coffig.spi.ConfigFunction;
import org.seedstack.coffig.spi.ConfigFunctionHolder;
import org.seedstack.shed.exception.SeedException;
import org.seedstack.seed.crypto.CryptoConfig;
import org.seedstack.seed.crypto.EncryptionService;

import javax.xml.bind.DatatypeConverter;
import java.security.InvalidKeyException;
import java.security.KeyStore;

public class DecryptFunction implements ConfigFunctionHolder {
    private EncryptionService encryptionService;

    @Override
    public void initialize(Coffig coffig) {
        CryptoConfig cryptoConfig = coffig.get(CryptoConfig.class);
        CryptoConfig.KeyStoreConfig masterKeyStoreConfig = cryptoConfig.masterKeyStore();
        if (masterKeyStoreConfig != null) {
            KeyStore keyStore = new KeyStoreLoader().load(CryptoPlugin.MASTER_KEYSTORE_NAME, masterKeyStoreConfig);
            CryptoConfig.KeyStoreConfig.AliasConfig aliasConfig = masterKeyStoreConfig.getAliases().get(CryptoPlugin.MASTER_KEY_NAME);
            if (aliasConfig == null || Strings.isNullOrEmpty(aliasConfig.getPassword())) {
                throw SeedException.createNew(CryptoErrorCode.MISSING_MASTER_KEY_PASSWORD);
            }
            encryptionService = new EncryptionServiceFactory(cryptoConfig, keyStore).create(CryptoPlugin.MASTER_KEY_NAME, aliasConfig.getPassword().toCharArray());
        } else {
            encryptionService = null;
        }
    }

    @ConfigFunction
    String decrypt(String key) {
        if (encryptionService == null) {
            throw SeedException.createNew(CryptoErrorCode.MISSING_MASTER_KEYSTORE).put("keyPassword", "${env.KEY_PASSWD}").put("password", "${env.KS_PASSWD}");
        }
        try {
            return new String(encryptionService.decrypt(DatatypeConverter.parseHexBinary(key)));
        } catch (InvalidKeyException e) {
            throw SeedException.wrap(e, CryptoErrorCode.UNEXPECTED_EXCEPTION);
        }
    }
}