/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
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

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

/**
 * Integration test for a {@link EncryptionService}. A new asymmetric key (key1) is defined in a property file
 * (certificate and private key in a keystore).
 *
 * @author thierry.bouvet@mpsa.com
 */
public class CryptoIT {

    @Inject
    private HashingService hashingService;

    @Inject
    @Named("client")
    private EncryptionService key1EncryptionService;

    @Inject
    @Named("ssl")
    private EncryptionService sslEncryptionService;

    @Inject
    @Named("database")
    private EncryptionService databaseEncryptionService;

    @Inject
    @Named("seed")
    private EncryptionService masterEncryptionService;

    @Rule
    public SeedITRule rule1 = new SeedITRule(this);

    /**
     * Set environment variables needed for the password lookup.
     *
     * @throws Exception if error occurred
     */
    @BeforeKernel
    public void beforeKernel() throws Exception {
        final Map<String, String> env = new HashMap<>(System.getenv());
        env.put("KS_PASSWD", "azerty");
        env.put("KEY_PASSWD", "azerty");
        new MockUp<System>() {
            @Mock
            public java.util.Map<String, String> getenv() {
                return env;
            }
        };
    }

    @Test
    public void testEncryption() throws Exception {
        final String chaine = "essai crypting";
        byte[] encrypt = key1EncryptionService.encrypt(chaine.getBytes());
        byte[] decrypt = key1EncryptionService.decrypt(encrypt);
        Assertions.assertThat(decrypt).isEqualTo(chaine.getBytes());
    }

    @Test
    public void testEncryptionWithMasterKey() throws Exception {
        final String chaine = "clientpasswd";
        byte[] encrypt = masterEncryptionService.encrypt(chaine.getBytes());
        byte[] decrypt = masterEncryptionService.decrypt(encrypt);
        Assertions.assertThat(decrypt).isEqualTo(chaine.getBytes());
    }

    @Test
    public void testEncryptionServiceInjectionWithCustomQualifier() {
        Assertions.assertThat(sslEncryptionService).isNotNull();
    }

    @Test
    public void test_hashing_service_injection() {
        Assertions.assertThat(hashingService).isNotNull();
        Hash hash = hashingService.createHash("string to hash");
        Assertions.assertThat(hash).isNotNull();
    }

}
