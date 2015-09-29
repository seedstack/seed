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
 * Creation : 10 juin 2015
 */
/**
 *
 */
package org.seedstack.seed.crypto.internal;

/**
 * Unit test for {@link PasswordLookup}.
 *
 * @author thierry.bouvet@mpsa.com
 */
public class PasswordLookupTest {

//    /**
//     * Test method for {@link org.seedstack.seed.crypto.internal.PasswordLookup#lookup(java.lang.String)}.
//     *
//     * @throws Exception if an error occurred
//     */
//    @Test
//    public void testLookupString(@Mocked final Application application, @Mocked final EncryptionService service, @Mocked final EncryptionServiceFactory serviceFactory) throws Exception {
//        final String toDecrypt = "essai crypting";
//        final String cryptingString = DatatypeConverter.printHexBinary(toDecrypt.getBytes());
//
//        new Expectations() {
//            {
//
//                serviceFactory.create();
//                result = service;
//
//                service.decrypt(DatatypeConverter.parseHexBinary(cryptingString));
//                result = toDecrypt.getBytes();
//            }
//        };
//        PasswordLookup lookup = new PasswordLookup(application);
//        //Deencapsulation.setField(lookup, "encryptionService", service);
//        Assertions.assertThat(lookup.lookup(cryptingString)).isEqualTo(toDecrypt);
//    }
//
//    /**
//     * Test method for {@link org.seedstack.seed.crypto.internal.PasswordLookup#lookup(java.lang.String)}.
//     *
//     * @throws Exception if an error occurred
//     */
//    @Test(expected = RuntimeException.class)
//    public void testLookupStringWithInvalidKey(@Mocked final EncryptionService service, @Mocked final EncryptionServiceFactory serviceFactory) throws Exception {
//        final String toDecrypt = "essai crypting";
//        final String cryptingString = DatatypeConverter.printHexBinary(toDecrypt.getBytes());
//
//        new Expectations() {
//            {
//                serviceFactory.create();
//                result = service;
//
//                service.decrypt(DatatypeConverter.parseHexBinary(cryptingString));
//                result = new InvalidKeyException("dummy exception");
//            }
//        };
//        PasswordLookup lookup = new PasswordLookup();
//        Deencapsulation.setField(lookup, "encryptionService", service);
//        lookup.lookup(cryptingString);
//    }

}
