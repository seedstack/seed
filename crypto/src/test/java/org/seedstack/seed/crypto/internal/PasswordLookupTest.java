/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
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

import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import org.apache.commons.configuration.Configuration;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.seedstack.seed.Application;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.crypto.EncryptionService;

import javax.xml.bind.DatatypeConverter;
import java.security.InvalidKeyException;
import java.security.KeyStore;

/**
 * Unit test for {@link PasswordLookup}.
 *
 * @author thierry.bouvet@mpsa.com
 */
public class PasswordLookupTest {

    private static final String toDecrypt = "essai crypting";
    private static final String cryptingString = DatatypeConverter.printHexBinary(toDecrypt.getBytes());

    @Mocked
    private Application application;
    @Mocked
    private Configuration configuration;
    @Mocked
    private EncryptionServiceFactory encryptionServiceFactory;
    @Mocked
    private KeyStoreConfigFactory keyStoreConfigFactory;
    @Mocked
    private KeyStoreLoader keyStoreLoader;

    @Injectable
    private EncryptionService encryptionService;
    @Injectable
    private KeyStore keyStore;

    @Test
    public void testLookupWithoutMasterKeyStore() throws Exception {
        PasswordLookup lookup = new PasswordLookup(application);
        Assertions.assertThat(lookup).isNotNull();
    }

    @Test(expected = SeedException.class)
    public void testLookupDecryptWithoutMasterKeyStore() throws Exception {
        PasswordLookup lookup = new PasswordLookup(application);
        lookup.lookup("");
    }

    @Test
    public void testLookupString() throws Exception {
        prepareLookup("name", "path/to/keystore", "password", "seed", "seedPassword");

        new Expectations() {
            {
                encryptionService.decrypt(DatatypeConverter.parseHexBinary(cryptingString));
                result = toDecrypt.getBytes();
            }
        };

        PasswordLookup lookup = new PasswordLookup(application);
        Assertions.assertThat(lookup.lookup(cryptingString)).isEqualTo(toDecrypt);
    }

    @Test(expected = SeedException.class)
    public void testLookupStringWithoutPassword() throws Exception {
        final KeyStoreConfig keyStoreConfig = new KeyStoreConfig("name", "path/to/keystore", "password");
        keyStoreConfig.addAliasPassword("seed", "");

        new Expectations() {
            {
                configuration.containsKey(CryptoPlugin.MASTER_KEYSTORE_PATH);
                result = true;

                keyStoreConfigFactory.create("master");
                result = keyStoreConfig;

                keyStoreLoader.load(keyStoreConfig);
                result = keyStore;
            }
        };
        new PasswordLookup(application);
    }

    @Test(expected = SeedException.class)
    public void testLookupStringWithInvalidKey() throws Exception {
        prepareLookup("name", "path/to/keystore", "password", "seed", "seedPassword");

        new Expectations() {
            {
                encryptionService.decrypt(DatatypeConverter.parseHexBinary(cryptingString));
                result = new InvalidKeyException("dummy exception");
            }
        };

        PasswordLookup lookup = new PasswordLookup(application);
        lookup.lookup(cryptingString);
    }

    private void prepareLookup(final String name, final String path, final String password,
                               final String alias, final String aliasPassword) throws InvalidKeyException {

        final KeyStoreConfig keyStoreConfig = new KeyStoreConfig(name, path, password);
        keyStoreConfig.addAliasPassword(alias, aliasPassword);

        new Expectations() {
            {
                configuration.containsKey(CryptoPlugin.MASTER_KEYSTORE_PATH);
                result = true;

                keyStoreConfigFactory.create("master");
                result = keyStoreConfig;

                keyStoreLoader.load(keyStoreConfig);
                result = keyStore;

                encryptionServiceFactory.create(alias, aliasPassword.toCharArray());
                result = encryptionService;
            }
        };
    }

}
