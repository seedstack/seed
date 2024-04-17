/*
 * Copyright © 2013-2024, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.crypto;

import com.google.common.io.BaseEncoding;
import org.seedstack.coffig.Coffig;
import org.seedstack.coffig.spi.ConfigFunction;
import org.seedstack.coffig.spi.ConfigFunctionHolder;
import org.seedstack.coffig.spi.ConfigurationComponent;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.crypto.CryptoConfig;
import org.seedstack.seed.crypto.EncryptionService;

import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public class DecryptFunction implements ConfigFunctionHolder {
    private final AtomicBoolean initInProgress = new AtomicBoolean(false);
    private volatile EncryptionService encryptionService;
    private Coffig coffig;

    @Override
    public void initialize(Coffig coffig) {
        this.coffig = coffig;
        try {
            initInProgress.set(true);
        } finally {
            initInProgress.set(false);
        }
    }

    @Override
    public ConfigurationComponent fork() {
        return new DecryptFunction();
    }

    @ConfigFunction
    String decrypt(String alias, String value) {
        if (encryptionService == null) {
            synchronized (this) {
                if (encryptionService == null) {
                    Optional<CryptoConfig.KeyStoreConfig> optional = coffig.getOptional(CryptoConfig.KeyStoreConfig.class, "crypto.keystores.master");
                    if (optional.isPresent()) {
                        CryptoConfig.KeyStoreConfig cfg = optional.get();
                        KeyStore keyStore = new KeyStoreLoader().load(CryptoConfig.MASTER_KEY_STORE_NAME, cfg);
                        EncryptionServiceFactory encryptionServiceFactory = new EncryptionServiceFactory(keyStore);
                        encryptionService = CryptoPlugin.getMasterEncryptionService(encryptionServiceFactory, cfg, alias);
                    } else {
                        throw SeedException.createNew(CryptoErrorCode.MISSING_MASTER_KEYSTORE);
                    }
                }
            }
        }
        return new String(encryptionService.decrypt(BaseEncoding.base16().decode(value)), StandardCharsets.UTF_8);
    }
}