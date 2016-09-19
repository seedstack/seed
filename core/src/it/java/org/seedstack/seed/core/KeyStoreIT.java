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
package org.seedstack.seed.core;

import org.assertj.core.api.Assertions;
import org.junit.Rule;
import org.junit.Test;
import org.seedstack.seed.crypto.EncryptionService;

import javax.inject.Inject;
import javax.inject.Named;
import java.security.KeyStore;

/**
 * Integration test for a {@link EncryptionService}. A new asymmetric key (key1) is defined in a property file
 * (certificate and private key in a keystore).
 *
 * @author thierry.bouvet@mpsa.com
 */
public class KeyStoreIT {
    @Rule
    public SeedITRule rule = new SeedITRule(this);
    @Inject
    @Named("default")
    private KeyStore defaultKeyStore;
    @Inject
    @Named("master")
    private KeyStore masterKeyStore;
    @Inject
    @Named("myKeyStore")
    private KeyStore myKeyStore;

    @Test
    public void testKeyStoresInjection() {
        Assertions.assertThat(defaultKeyStore).isNotNull();
        Assertions.assertThat(masterKeyStore).isNotNull();
        Assertions.assertThat(myKeyStore).isNotNull();
    }
}
