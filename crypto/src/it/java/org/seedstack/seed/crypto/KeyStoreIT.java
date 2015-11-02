/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
/*
 * Creation : 11 juin 2015
 */
package org.seedstack.seed.crypto;

import mockit.Mock;
import mockit.MockUp;
import org.assertj.core.api.Assertions;
import org.junit.Rule;
import org.junit.Test;
import org.seedstack.seed.crypto.api.EncryptionService;

import javax.inject.Inject;
import javax.inject.Named;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;

/**
 * Integration test for a {@link EncryptionService}. A new asymmetric key (key1) is defined in a property file
 * (certificate and private key in a keystore).
 *
 * @author thierry.bouvet@mpsa.com
 */
public class KeyStoreIT {

    @Inject
    @Named("default")
    private KeyStore defaultKeyStore;

    @Inject
    @Named("master")
    private KeyStore masterKeyStore;

    @Inject
    @Named("keystoreName1")
    private KeyStore keyStore1;

    @Inject
    @Named("keystoreName2")
    private KeyStore keyStore2;

    @Rule
    public SeedITRule rule1 = new SeedITRule(this);

    /**
     * Set environment variables needed for the password lookup.
     *
     * @throws Exception if error occurred
     */
    @BeforeKernel
    public void beforeKernel() throws Exception {
        final Map<String, String> env = new HashMap<String, String>(System.getenv());
        env.put("KS_PASSWD", "azerty");
        env.put("KEY_PASSWD", "azerty");
        new MockUp<System>() {
            @Mock
            public Map<String, String> getenv() {
                return env;
            }
        };
    }

    @Test
    public void testKeyStoresInjection() {
        Assertions.assertThat(defaultKeyStore).isNotNull();
        Assertions.assertThat(masterKeyStore).isNotNull();
        Assertions.assertThat(keyStore1).isNotNull();
        Assertions.assertThat(keyStore2).isNotNull();
    }

}
