/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
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

import mockit.*;
import org.apache.commons.configuration.Configuration;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.crypto.EncryptionService;

import java.io.FileInputStream;
import java.net.URL;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

import static org.seedstack.seed.core.utils.ConfigurationUtils.buildKey;

/**
 * Unit test for {@link EncryptionServiceFactory}.
 *
 * @author thierry.bouvet@mpsa.com
 */
public class EncryptionServiceFactoryTest {

    private static final String ALIAS = "key1";
    public static final String CERT_FILE_KEY = buildKey("cert", ALIAS, "file");
    public static final String CERT_RESOURCE_KEY = buildKey("cert", ALIAS, "resource");
    private static final char[] PASSWORD = "password".toCharArray();

    @Mocked
    private Configuration configuration;
    @Mocked
    private KeyStore keyStore;
    @Mocked
    private Key key;
    @Mocked
    private Certificate certificate;
    @Mocked
    private PublicKey publicKey;

    @Test
    public void testCreateEncryptionService() throws Exception {
        new Expectations() {
            {
                keyStore.getKey(ALIAS, PASSWORD);
                result = key;
                keyStore.getCertificate(ALIAS);
                result = certificate;
                certificate.getPublicKey();
                result = publicKey;
            }
        };

        KeyPairConfig keyPairConfig = new KeyPairConfig("keyStoreName", ALIAS, "password", null, null);
        EncryptionServiceFactory factory = new EncryptionServiceFactory(configuration, keyStore);
        EncryptionService encryptionService = factory.create(ALIAS, PASSWORD);

        Assertions.assertThat(encryptionService).isNotNull();

        new Verifications() {
            {
                new EncryptionServiceImpl(ALIAS, publicKey, key);
            }
        };
    }

    @Test
    public void testCreateForAliasWithoutPrivateKey() throws KeyStoreException {
        new Expectations() {
            {
                keyStore.getCertificate(ALIAS);
                result = null;
            }
        };

        EncryptionServiceFactory factory = new EncryptionServiceFactory(configuration, keyStore);
        EncryptionService encryptionService = factory.create(ALIAS);

        Assertions.assertThat(encryptionService).isNotNull();

        new Verifications() {
            {
                new EncryptionServiceImpl(ALIAS, publicKey, null);
            }
        };
    }

    @Test(expected = SeedException.class)
    public void testCreateWithMissingAliasPassword() throws Exception {
        new Expectations() {
            {
                keyStore.getKey(ALIAS, null);
                result = new UnrecoverableKeyException();
            }
        };
        new EncryptionServiceFactory(configuration, keyStore).create(ALIAS, null);
    }

    @Test
    public void testCreateForAliasWithoutCertificate() throws Exception {
        new Expectations() {
            {
                keyStore.getKey(ALIAS, PASSWORD);
                result = key;
                keyStore.getCertificate(ALIAS);
                result = null;
            }
        };

        EncryptionServiceFactory factory = new EncryptionServiceFactory(configuration, keyStore);
        EncryptionService encryptionService = factory.create(ALIAS, PASSWORD);

        Assertions.assertThat(encryptionService).isNotNull();

        new Verifications() {
            {
                new EncryptionServiceImpl(ALIAS, publicKey, key);
            }
        };
    }

    @Test
    public void testCreateWithExternalCertificateFromFile(@Mocked final FileInputStream fileInputStream, @Mocked CertificateFactory certificateFactory) throws Exception {

        new Expectations() {
            {
                keyStore.getKey(ALIAS, PASSWORD);
                result = key;

                certificate.getPublicKey();
                result = publicKey;

                configuration.containsKey(CERT_FILE_KEY);
                result = true;
                configuration.getString(CERT_FILE_KEY);
                result = "path/to/cert";

                new FileInputStream("path/to/cert");
                result = fileInputStream;
            }
        };

        EncryptionServiceFactory factory = new EncryptionServiceFactory(configuration, keyStore);
        EncryptionService encryptionService = factory.create(ALIAS, PASSWORD);

        Assertions.assertThat(encryptionService).isNotNull();

        new Verifications() {
            {
                new EncryptionServiceImpl(ALIAS, publicKey, key);
            }
        };
    }

    @Test
    public void testCreateWithExternalCertificateFromResource(@Mocked CertificateFactory certificateFactory, @Mocked final URL url) throws Exception {
        new MockUp<ClassLoader> (){
            @Mock
            public URL getResource(String name) {
                return url;
            }
        };

        new Expectations() {
            {
                keyStore.getKey(ALIAS, PASSWORD);
                result = key;

                certificate.getPublicKey();
                result = publicKey;

                configuration.containsKey(CERT_RESOURCE_KEY);
                result = true;
            }
        };

        EncryptionServiceFactory factory = new EncryptionServiceFactory(configuration, keyStore);
        EncryptionService encryptionService = factory.create(ALIAS, PASSWORD);

        Assertions.assertThat(encryptionService).isNotNull();

        new Verifications() {
            {
                new EncryptionServiceImpl(ALIAS, publicKey, key);
            }
        };
    }

    @Test(expected = SeedException.class)
    public void testMissingCertificateFromResource() throws Exception {

        new Expectations() {
            {
                configuration.containsKey(CERT_RESOURCE_KEY);
                result = true;
                configuration.getString(CERT_RESOURCE_KEY);
                result = "path/to/cert";
            }
        };

        new EncryptionServiceFactory(configuration, keyStore).create(ALIAS, PASSWORD);
    }

}
