/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.internal.crypto;

import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.seedstack.seed.SeedException;

/**
 * Unit test for {@link EncryptionServiceImpl}.
 */
@RunWith(JMockit.class)
public class EncryptionServiceImplTest {

    /**
     * Test method for {@link EncryptionServiceImpl#encrypt(byte[])}. Test encrypt without any problem.
     *
     * @throws Exception if an error occurred
     */
    @Test
    public void testEncrypt(@Mocked final PublicKey publicKey, @Mocked final Cipher cipher)
            throws Exception {

        EncryptionServiceImpl asymetricCrypting = new EncryptionServiceImpl("alias", publicKey, null);
        final String toCrypt = "text to crypt";
        asymetricCrypting.encrypt(toCrypt.getBytes());

        new Verifications() {
            {
                cipher.doFinal(toCrypt.getBytes());
                times = 1;
            }
        };
    }

    /**
     * Test method for {@link EncryptionServiceImpl#encrypt(byte[])}. Test encrypt without any problem.
     *
     * @throws Exception if an error occurred
     */
    @Test(expected = SeedException.class)
    public void testEncryptWithoutPublicKey() throws Exception {
        EncryptionServiceImpl asymetricCrypting = new EncryptionServiceImpl("alias", null, null);
        final String toCrypt = "text to crypt";
        asymetricCrypting.encrypt(toCrypt.getBytes());
    }

    /**
     * Test method for {@link EncryptionServiceImpl#encrypt(byte[])}. Test encrypt Cipher problem.
     *
     * @throws Exception if an error occurred
     */
    @Test(expected = SeedException.class)
    public void testEncryptWithNoSuchAlgorithmException(@Mocked final PublicKey publicKey,
            @Mocked final Cipher cipher) throws Exception {

        new Expectations() {
            {
                Cipher.getInstance("RSA/ECB/PKCS1PADDING");
                result = new NoSuchAlgorithmException("dummy exception");
            }
        };
        EncryptionServiceImpl asymetricCrypting = new EncryptionServiceImpl("alias", publicKey, null);
        final String toCrypt = "text to crypt";
        asymetricCrypting.encrypt(toCrypt.getBytes());

    }

    /**
     * Test method for {@link EncryptionServiceImpl#encrypt(byte[])}. Test encrypt Cipher problem.
     *
     * @throws Exception if an error occurred
     */
    @Test(expected = SeedException.class)
    public void testEncryptWithNoSuchPaddingException(@Mocked final PublicKey publicKey,
            @Mocked final Cipher cipher) throws Exception {

        new Expectations() {
            {
                Cipher.getInstance("RSA/ECB/PKCS1PADDING");
                result = new NoSuchPaddingException("dummy exception");
            }
        };
        EncryptionServiceImpl asymetricCrypting = new EncryptionServiceImpl("alias", publicKey, null);
        final String toCrypt = "text to crypt";
        asymetricCrypting.encrypt(toCrypt.getBytes());
    }

    /**
     * Test method for {@link EncryptionServiceImpl#encrypt(byte[])}. Test encrypt Cipher problem.
     *
     * @throws Exception if an error occurred
     */
    @Test(expected = SeedException.class)
    public void testEncryptWithIllegalBlockSizeException(@Mocked final PublicKey publicKey,
            @Mocked final Cipher cipher) throws Exception {

        final String toCrypt = "text to crypt";

        new Expectations() {
            {
                Cipher.getInstance("RSA/ECB/PKCS1PADDING");
                result = cipher;

                cipher.doFinal(toCrypt.getBytes());
                result = new IllegalBlockSizeException("dummy exception");
            }
        };
        EncryptionServiceImpl asymetricCrypting = new EncryptionServiceImpl("alias", publicKey, null);
        asymetricCrypting.encrypt(toCrypt.getBytes());
    }

    /**
     * Test method for {@link EncryptionServiceImpl#encrypt(byte[])}. Test encrypt Cipher problem.
     *
     * @throws Exception if an error occurred
     */
    @Test(expected = SeedException.class)
    public void testEncryptWithBadPaddingException(@Mocked final PublicKey publicKey,
            @Mocked final Cipher cipher) throws Exception {

        final String toCrypt = "text to crypt";

        new Expectations() {
            {
                Cipher.getInstance("RSA/ECB/PKCS1PADDING");
                result = cipher;

                cipher.doFinal(toCrypt.getBytes());
                result = new BadPaddingException("dummy exception");
            }
        };
        EncryptionServiceImpl asymetricCrypting = new EncryptionServiceImpl("alias", publicKey, null);
        asymetricCrypting.encrypt(toCrypt.getBytes());

    }

    /**
     * Test method for {@link EncryptionServiceImpl#decrypt(byte[])}.
     *
     * @throws Exception if an error occurred
     */
    @Test
    public void testDecrypt(@Mocked final Key key, @Mocked final Cipher cipher)
            throws Exception {
        final String toDecrypt = "ADEF0985C";

        EncryptionServiceImpl asymetricCrypting = new EncryptionServiceImpl("alias", null, key);
        asymetricCrypting.decrypt(toDecrypt.getBytes());
        new Verifications() {
            {
                cipher.doFinal(toDecrypt.getBytes());
                times = 1;
            }
        };

    }

    /**
     * Test method for {@link EncryptionServiceImpl#decrypt(byte[])}. Test without keystore so it's not possible.
     *
     * @throws Exception if an error occurred
     */
    @Test(expected = SeedException.class)
    public void testDecryptWithoutPrivateKey() throws Exception {
        final String toDecrypt = "ADEF0985C";

        EncryptionServiceImpl asymetricCrypting = new EncryptionServiceImpl("alias", null, null);
        asymetricCrypting.decrypt(toDecrypt.getBytes());

    }
}
