/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
/*
 * Creation : 10 juin 2015
 */
/**
 * 
 */
package org.seedstack.seed.crypto.internal;

import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.name.Names;
import mockit.Mocked;
import mockit.Verifications;
import org.junit.Test;
import org.seedstack.seed.crypto.api.EncryptionService;

import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;

/**
 * Unit test for {@link CryptoModule}
 * 
 * @author thierry.bouvet@mpsa.com
 */
public class CryptoModuleTest {

    /**
     * Test method for {@link org.seedstack.seed.crypto.internal.CryptoModule#configure()}. This method inject a mocked
     * {@link Binder} to check if bind is ok.
     */
    @Test
    public void testConfigure(@Mocked final EncryptionServiceImpl asymetricCryptingRSA, @Mocked final KeyStore keyStore, @Mocked final Binder binder) {

        final Map<String, KeyStore> keyStores = new HashMap<String, KeyStore>();
        keyStores.put("k1", keyStore);
        final Map<String, EncryptionService> rsaServices = new HashMap<String, EncryptionService>();
        rsaServices.put("k1", asymetricCryptingRSA);
        rsaServices.put("k2", asymetricCryptingRSA);

        CryptoModule module = new CryptoModule(rsaServices, keyStores);
        module.configure(binder);
        new Verifications() {
            {
                binder.bind(Key.get(EncryptionService.class, Names.named("k1"))).toInstance(asymetricCryptingRSA);
                times = 1;
                binder.bind(Key.get(EncryptionService.class, Names.named("k2"))).toInstance(asymetricCryptingRSA);
                times = 1;
                binder.bind(Key.get(KeyStore.class, Names.named("k1"))).toInstance(keyStore);
                times = 1;
            }
        };
    }

}
