/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
/*
 * Creation : 4 juin 2015
 */
/**
 *
 */
package org.seedstack.seed.crypto.internal;

import com.google.inject.Key;
import mockit.Deencapsulation;
import mockit.Mocked;
import mockit.Verifications;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.seedstack.seed.core.internal.application.ApplicationPlugin;
import org.seedstack.seed.crypto.EncryptionService;

import java.security.KeyStore;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Unit test for {@link CryptoPlugin}.
 *
 * @author thierry.bouvet@mpsa.com
 */
public class CryptoPluginTest {

    @Test
    public void testName() {
        Assertions.assertThat(new CryptoPlugin().name()).isEqualTo("seed-crypto-plugin");
    }

    @Test
    public void testNativeUnitModule(@SuppressWarnings("unused") @Mocked final CryptoModule module) {
        final Map<Key<EncryptionService>, EncryptionService> encryptionServices = new HashMap<Key<EncryptionService>, EncryptionService>();
        final Map<String, KeyStore> keyStores = new HashMap<String, KeyStore>();
        final CryptoPlugin underTest = new CryptoPlugin();
        Deencapsulation.setField(underTest, "encryptionServices", encryptionServices);
        Deencapsulation.setField(underTest, "keyStores", keyStores);

        underTest.nativeUnitModule();

        new Verifications() {{
            new CryptoModule(encryptionServices, keyStores);
            times = 1;
        }};
    }

    @Test
    public void testRequiredPlugins() {
        CryptoPlugin plugin = new CryptoPlugin();
        Collection<Class<?>> list = plugin.requiredPlugins();
        Assertions.assertThat(list.contains(ApplicationPlugin.class)).isTrue();
    }
}
