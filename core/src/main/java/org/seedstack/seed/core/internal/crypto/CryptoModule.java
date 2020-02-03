/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.crypto;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.multibindings.OptionalBinder;
import com.google.inject.name.Names;
import com.google.inject.util.Providers;
import java.security.KeyStore;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509KeyManager;
import org.seedstack.seed.crypto.EncryptionService;
import org.seedstack.seed.crypto.HashingService;

class CryptoModule extends AbstractModule {
    private static final String TRUSTSTORE = "truststore";
    private final Map<Key<EncryptionService>, EncryptionService> encryptionServices;
    private final KeyStore trustStore;
    private final Map<String, KeyStore> keyStores;
    private final SSLContext sslContext;
    private final List<KeyManagerAdapter> keyManagerAdapters;
    private final Class<? extends X509KeyManager> keyManagerClass;

    CryptoModule(Map<Key<EncryptionService>, EncryptionService> encryptionServices, Map<String, KeyStore> keyStores,
            KeyStore trustStore, SSLContext sslContext,
            List<KeyManagerAdapter> keyManagerAdapters,
            Class<? extends X509KeyManager> keyManagerClass) {
        this.encryptionServices = encryptionServices;
        this.trustStore = trustStore;
        this.keyStores = keyStores;
        this.sslContext = sslContext;
        this.keyManagerAdapters = keyManagerAdapters;
        this.keyManagerClass = keyManagerClass;
    }

    @Override
    protected void configure() {
        // Truststore
        OptionalBinder.newOptionalBinder(binder(), Key.get(KeyStore.class, Names.named(TRUSTSTORE)));
        if (trustStore != null) {
            bind(KeyStore.class).annotatedWith(Names.named(TRUSTSTORE)).toInstance(trustStore);
        }

        // Keystore(s)
        for (Entry<String, KeyStore> keyStoreEntry : this.keyStores.entrySet()) {
            bind(Key.get(KeyStore.class, Names.named(keyStoreEntry.getKey()))).toInstance(keyStoreEntry.getValue());
        }

        // Encryption service(s)
        for (Entry<Key<EncryptionService>, EncryptionService> entry : this.encryptionServices.entrySet()) {
            bind(entry.getKey()).toInstance(entry.getValue());
        }

        // Hashing service
        bind(HashingService.class).to(PBKDF2HashingService.class);

        // SSL context
        OptionalBinder.newOptionalBinder(binder(), SSLContext.class);
        if (sslContext != null) {
            bind(SSLContext.class).toInstance(sslContext);
        }

        // Bind custom X509KeyManager if any
        if (keyManagerClass != null) {
            bind(X509KeyManager.class).to(keyManagerClass);
        } else {
            bind(X509KeyManager.class).toProvider(Providers.of(null));
        }

        // KeyManager adapters should be injectable
        keyManagerAdapters.forEach(this::requestInjection);
    }
}
