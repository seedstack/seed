/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.internal.crypto;

import com.google.inject.Key;
import java.security.KeyStore;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.net.ssl.SSLContext;
import mockit.Deencapsulation;
import mockit.Mocked;
import mockit.Verifications;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.seedstack.seed.crypto.EncryptionService;
import org.seedstack.seed.spi.ApplicationProvider;

/**
 * Unit test for {@link CryptoPlugin}.
 */
public class CryptoPluginTest {

    @Test
    public void testName() {
        Assertions.assertThat(new CryptoPlugin().name()).isEqualTo("crypto");
    }

    @Test
    public void testNativeUnitModule(@Mocked final CryptoModule module, @Mocked final SSLContext sslContext) {
        final Map<Key<EncryptionService>, EncryptionService> encryptionServices = new HashMap<>();
        final Map<String, KeyStore> keyStores = new HashMap<>();
        final CryptoPlugin underTest = new CryptoPlugin();
        Deencapsulation.setField(underTest, "encryptionServices", encryptionServices);
        Deencapsulation.setField(underTest, "keyStores", keyStores);
        Deencapsulation.setField(underTest, "sslContext", sslContext);

        underTest.nativeUnitModule();

        new Verifications() {{
            new CryptoModule(encryptionServices, keyStores, sslContext);
            times = 1;
        }};
    }

    @Test
    public void testRequiredPlugins() {
        CryptoPlugin plugin = new CryptoPlugin();
        Collection<Class<?>> list = plugin.requiredPlugins();
        Assertions.assertThat(list.contains(ApplicationProvider.class)).isTrue();
    }
}
