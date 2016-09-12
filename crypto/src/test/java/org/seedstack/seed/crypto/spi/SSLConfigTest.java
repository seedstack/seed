/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
///**
// * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
// *
// * This Source Code Form is subject to the terms of the Mozilla Public
// * License, v. 2.0. If a copy of the MPL was not distributed with this
// * file, You can obtain one at http://mozilla.org/MPL/2.0/.
// */
//package org.seedstack.seed.crypto.spi;
//
//import org.assertj.core.api.Assertions;
//import org.junit.Test;
//import org.seedstack.seed.SeedException;
//
///**
// * Tests SSL configuration initialization.
// *
// * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
// */
//public class SSLConfigTest {
//
//    @Test
//    public void test_ssl_configuration_default_values() {
//        SSLConfig sslConfiguration = new SSLConfig(null, null, null);
//        Assertions.assertThat(sslConfiguration.getProtocol()).isEqualTo("TLS");
//        Assertions.assertThat(sslConfiguration.getClientAuthMode()).isEqualTo(SSLAuthenticationMode.NOT_REQUESTED);
//        Assertions.assertThat(sslConfiguration.getCiphers()).isNull();
//    }
//
//    @Test
//    public void test_ssl_configuration_empty_values() {
//        SSLConfig sslConfiguration = new SSLConfig("", "", null);
//        Assertions.assertThat(sslConfiguration.getProtocol()).isEqualTo("TLS");
//        Assertions.assertThat(sslConfiguration.getClientAuthMode()).isEqualTo(SSLAuthenticationMode.NOT_REQUESTED);
//        Assertions.assertThat(sslConfiguration.getCiphers()).isNull();
//    }
//
//    @Test
//    public void test_ssl_configuration() {
//        SSLConfig sslConfiguration = new SSLConfig("proto", "REQUESTED", new String[]{"cipher1", "cipher2"});
//        Assertions.assertThat(sslConfiguration.getProtocol()).isEqualTo("proto");
//        Assertions.assertThat(sslConfiguration.getClientAuthMode()).isEqualTo(SSLAuthenticationMode.REQUESTED);
//        Assertions.assertThat(sslConfiguration.getCiphers()).containsOnly("cipher1", "cipher2");
//
//        sslConfiguration = new SSLConfig("proto", "REQUIRED", new String[]{"cipher1", "cipher2"});
//        Assertions.assertThat(sslConfiguration.getClientAuthMode()).isEqualTo(SSLAuthenticationMode.REQUIRED);
//
//        sslConfiguration = new SSLConfig("proto", "NOT_REQUESTED", new String[]{"cipher1", "cipher2"});
//        Assertions.assertThat(sslConfiguration.getClientAuthMode()).isEqualTo(SSLAuthenticationMode.NOT_REQUESTED);
//    }
//
//    @Test(expected = SeedException.class)
//    public void test_ssl_configuration_wrong_value() {
//        new SSLConfig(null, "wrong value", null);
//    }
//}
