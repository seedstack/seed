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
 * Creation : 11 juin 2015
 */
package org.seedstack.seed.crypto;

import mockit.Mock;
import mockit.MockUp;
import org.assertj.core.api.Assertions;
import org.junit.Rule;
import org.junit.Test;
import org.seedstack.seed.crypto.api.EncryptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.bind.DatatypeConverter;
import java.util.HashMap;
import java.util.Map;

/**
 * Integration test for a {@link EncryptionService}. A new asymmetric key (key1) is defined in a property file (certificate and private key in a
 * keystore).
 *
 * @author thierry.bouvet@mpsa.com
 */
public class CryptoIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(CryptoIT.class);

    @Inject
    @Named("client")
    private EncryptionService key1EncryptionService;

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
        LOGGER.info("run beforeKernel function");
        final Map<String, String> env = new HashMap<String, String>(System.getenv());
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
    public void testCustomAsymetricKey() throws Exception {
        LOGGER.info("Test crypt with X509");
        final String chaine = "essai crypting";
        byte[] encrypt = key1EncryptionService.encrypt(chaine.getBytes());
        LOGGER.info("String crypt: {}", DatatypeConverter.printHexBinary(encrypt));
        byte[] decrypt = key1EncryptionService.decrypt(encrypt);
        LOGGER.info("String decrypt: {}", new String(decrypt));
        Assertions.assertThat(decrypt).isEqualTo(chaine.getBytes());
    }

    @Test
    public void testWithMasterKey() throws Exception {
        LOGGER.info("Test crypt with X509 internal key");
        final String chaine = "clientpasswd";
        byte[] encrypt = masterEncryptionService.encrypt(chaine.getBytes());
        LOGGER.info("String crypt: {}", DatatypeConverter.printHexBinary(encrypt));
        byte[] decrypt = masterEncryptionService.decrypt(encrypt);
        LOGGER.info("String decrypt: {}", new String(decrypt));
        Assertions.assertThat(decrypt).isEqualTo(chaine.getBytes());
    }

}
