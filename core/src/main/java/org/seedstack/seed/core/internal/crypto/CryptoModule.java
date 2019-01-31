/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.crypto;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.name.Names;
import java.security.KeyStore;
import java.util.Map;
import java.util.Map.Entry;
import javax.net.ssl.SSLContext;
import org.seedstack.seed.crypto.EncryptionService;
import org.seedstack.seed.crypto.HashingService;

class CryptoModule extends AbstractModule {
    private final Map<Key<EncryptionService>, EncryptionService> encryptionServices;
    private final Map<String, KeyStore> keyStores;
    private final SSLContext sslContext;

    CryptoModule(Map<Key<EncryptionService>, EncryptionService> encryptionServices, Map<String, KeyStore> keyStores,
            SSLContext sslContext) {
        this.encryptionServices = encryptionServices;
        this.keyStores = keyStores;
        this.sslContext = sslContext;
    }

    @Override
    protected void configure() {
        for (Entry<String, KeyStore> keyStoreEntry : this.keyStores.entrySet()) {
            bind(Key.get(KeyStore.class, Names.named(keyStoreEntry.getKey()))).toInstance(keyStoreEntry.getValue());
        }

        for (Entry<Key<EncryptionService>, EncryptionService> entry : this.encryptionServices.entrySet()) {
            bind(entry.getKey()).toInstance(entry.getValue());
        }

        bind(HashingService.class).to(PBKDF2HashingService.class);

        if (sslContext != null) {
            bind(SSLContext.class).toInstance(sslContext);
        }
    }
}
