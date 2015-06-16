/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
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

import java.util.HashMap;
import java.util.Map;

import mockit.Mocked;
import mockit.Verifications;

import org.junit.Test;
import org.seedstack.seed.crypto.api.EncryptionService;

import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.name.Names;

/**
 * Unit test for {@link CryptoModule}
 * 
 * @author thierry.bouvet@mpsa.com
 */
public class AsymetricCryptingSupportModuleTest {

    /**
     * Test method for {@link org.seedstack.seed.crypto.internal.CryptoModule#configure()}. This method inject a mocked
     * {@link Binder} to check if bind is ok.
     */
    @Test
    public void testConfigure(@Mocked final EncryptionServiceImpl asymetricCryptingRSA, @Mocked final Binder binder) {
        Map<String, EncryptionService> rsaServices = new HashMap<String, EncryptionService>();
        final String key = "keyname";
        rsaServices.put(key, asymetricCryptingRSA);

        CryptoModule module = new CryptoModule(rsaServices);
        module.configure(binder);
        new Verifications() {
            {
                binder.bind(Key.get(EncryptionService.class, Names.named(key))).toInstance(asymetricCryptingRSA);
                times = 1;
            }
        };
    }

}
