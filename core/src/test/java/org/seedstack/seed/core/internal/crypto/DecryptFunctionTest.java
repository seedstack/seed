/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.internal.crypto;

import com.google.common.io.BaseEncoding;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.integration.junit4.JMockit;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.seedstack.coffig.Coffig;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.crypto.CryptoConfig;
import org.seedstack.seed.crypto.EncryptionService;

/**
 * Unit test for {@link DecryptFunction}.
 */
@RunWith(JMockit.class)
public class DecryptFunctionTest {
    private static final String toDecrypt = "essai crypting";
    private static final String cryptingString = BaseEncoding.base16().encode(toDecrypt.getBytes());

    @Mocked
    private EncryptionServiceFactory encryptionServiceFactory;
    @Mocked
    private KeyStoreLoader keyStoreLoader;
    @Injectable
    private EncryptionService encryptionService;
    @Injectable
    private KeyStore keyStore;

    @Test
    public void testLookupWithoutMasterKeyStore() throws Exception {
        DecryptFunction decryptFunction = new DecryptFunction();
        decryptFunction.initialize(Coffig.builder().build());
        Assertions.assertThat(decryptFunction).isNotNull();
    }

    @Test(expected = SeedException.class)
    public void testLookupDecryptWithoutMasterKeyStore() throws Exception {
        DecryptFunction decryptFunction = new DecryptFunction();
        decryptFunction.initialize(Coffig.builder().build());
        decryptFunction.decrypt("seed", "");
    }

    @Test
    public void testLookupString(@Mocked CryptoConfig cryptoConfig) throws Exception {
        new Expectations() {{
            cryptoConfig.masterKeyStore();
            result = new CryptoConfig.KeyStoreConfig().addAlias("seed",
                    new CryptoConfig.KeyStoreConfig.AliasConfig().setPassword("toto"));

            encryptionService.decrypt(BaseEncoding.base16().decode(cryptingString));
            result = toDecrypt.getBytes();
        }};

        DecryptFunction decryptFunction = new DecryptFunction();
        decryptFunction.initialize(Coffig.builder().build());
        Assertions.assertThat(decryptFunction.decrypt("seed", cryptingString)).isEqualTo(toDecrypt);
    }

    @Test(expected = SeedException.class)
    public void testLookupStringWithoutPassword(@Mocked CryptoConfig cryptoConfig) throws Exception {
        CryptoConfig.KeyStoreConfig keyStoreConfig = new CryptoConfig.KeyStoreConfig().addAlias("seed",
                new CryptoConfig.KeyStoreConfig.AliasConfig());

        new Expectations() {{
            cryptoConfig.masterKeyStore();
            result = keyStoreConfig;

            keyStoreLoader.load("master", keyStoreConfig);
            result = keyStore;
        }};
        DecryptFunction decryptFunction = new DecryptFunction();
        decryptFunction.initialize(Coffig.builder().build());
        decryptFunction.decrypt("seed", cryptingString);
    }

    @Test(expected = SeedException.class)
    public void testLookupStringWithInvalidKey(@Mocked CryptoConfig cryptoConfig) throws Exception {
        CryptoConfig.KeyStoreConfig keyStoreConfig = new CryptoConfig.KeyStoreConfig().addAlias("seed",
                new CryptoConfig.KeyStoreConfig.AliasConfig().setPassword("seedPassword"));

        new Expectations() {{
            cryptoConfig.masterKeyStore();
            result = keyStoreConfig;

            encryptionService.decrypt(BaseEncoding.base16().decode(cryptingString));
            result = SeedException.wrap(new InvalidKeyException("dummy exception"), CryptoErrorCode.INVALID_KEY);
        }};

        DecryptFunction decryptFunction = new DecryptFunction();
        decryptFunction.initialize(Coffig.builder().build());
        decryptFunction.decrypt("seed", cryptingString);
    }
}
