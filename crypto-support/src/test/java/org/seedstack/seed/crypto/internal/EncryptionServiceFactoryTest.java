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

import mockit.Mocked;
import mockit.Verifications;
import org.junit.Test;
import org.seedstack.seed.core.api.Application;

import java.security.Key;
import java.security.KeyStore;
import java.security.PublicKey;

/**
 * Unit test for {@link EncryptionServiceFactory}.
 *
 * @author thierry.bouvet@mpsa.com
 */
public class EncryptionServiceFactoryTest {

    @Test
    public void testCreateEncryptionService(@Mocked final Application application, @Mocked final KeyStore keyStore,
                                            @Mocked final Key key, @Mocked final PublicKey publicKey) throws Exception {

        EncryptionServiceFactory factory = new EncryptionServiceFactory(application, keyStore);
        final char[] password = "password".toCharArray();
        factory.create("key1", password);

        new Verifications() {
            {
                new EncryptionServiceImpl("key1", publicKey, key);
            }
        };
    }

}
