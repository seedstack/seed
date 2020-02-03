/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core;

import java.security.KeyStore;
import javax.inject.Inject;
import javax.inject.Named;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.seedstack.seed.testing.junit4.SeedITRunner;

@RunWith(SeedITRunner.class)
public class KeyStoreIT {
    @Inject
    @Named("ssl")
    private KeyStore sslKeyStore;
    @Inject
    @Named("master")
    private KeyStore masterKeyStore;

    @Test
    public void testKeyStoresInjection() {
        Assertions.assertThat(sslKeyStore).isNotNull();
        Assertions.assertThat(masterKeyStore).isNotNull();
    }
}
