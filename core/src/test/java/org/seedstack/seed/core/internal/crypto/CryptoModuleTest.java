/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.crypto;

import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.name.Names;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;
import javax.net.ssl.SSLContext;
import mockit.Mocked;
import mockit.Verifications;
import org.junit.Test;
import org.seedstack.seed.crypto.EncryptionService;

/**
 * Unit test for {@link CryptoModule}
 */
public class CryptoModuleTest {

    /**
     * Test method for {@link CryptoModule#configure()}. This method inject a mocked
     * {@link Binder} to check if bind is ok.
     */
    @Test
    public void testConfigure(@Mocked final EncryptionServiceImpl asymetricCryptingRSA, @Mocked final KeyStore keyStore,
            @Mocked final Binder binder, @Mocked final SSLContext sslContext) {

        final Map<String, KeyStore> keyStores = new HashMap<>();
        keyStores.put("k1", keyStore);
        final Map<Key<EncryptionService>, EncryptionService> rsaServices = new HashMap<>();
        rsaServices.put(Key.get(EncryptionService.class, Names.named("k1")), asymetricCryptingRSA);
        rsaServices.put(Key.get(EncryptionService.class, Names.named("k2")), asymetricCryptingRSA);

        CryptoModule module = new CryptoModule(rsaServices, keyStores, sslContext);
        module.configure(binder);
        new Verifications() {
            {
                binder.bind(Key.get(EncryptionService.class, Names.named("k1"))).toInstance(asymetricCryptingRSA);
                times = 1;
                binder.bind(Key.get(EncryptionService.class, Names.named("k2"))).toInstance(asymetricCryptingRSA);
                times = 1;
                binder.bind(Key.get(KeyStore.class, Names.named("k1"))).toInstance(keyStore);
                times = 1;
                binder.bind(SSLContext.class).toInstance(sslContext);
                times = 1;
            }
        };
    }

}
