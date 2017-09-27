/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core;

import javax.inject.Inject;
import javax.inject.Named;
import org.assertj.core.api.Assertions;
import org.junit.Rule;
import org.junit.Test;
import org.seedstack.seed.core.rules.SeedITRule;
import org.seedstack.seed.crypto.EncryptionService;
import org.seedstack.seed.crypto.Hash;
import org.seedstack.seed.crypto.HashingService;

/**
 * Integration test for a {@link EncryptionService}. A new asymmetric key (key1) is defined in a property file
 * (certificate and private key in a keystore).
 */
public class CryptoIT {

    @Rule
    public SeedITRule rule = new SeedITRule(this);
    @Inject
    private HashingService hashingService;
    @Inject
    @Named("client")
    private EncryptionService key1EncryptionService;
    @Inject
    @Named("ssl")
    private EncryptionService sslEncryptionService;
    @Inject
    @Named("seed")
    private EncryptionService masterEncryptionService;

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
