/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.crypto.internal;

import mockit.Expectations;
import mockit.Mocked;
import org.apache.commons.configuration.Configuration;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.seedstack.seed.crypto.spi.SSLAuthenticationMode;
import org.seedstack.seed.crypto.spi.SSLConfiguration;

import static org.seedstack.seed.crypto.internal.SSLConfigFactory.*;

/**
 * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
 */
public class SSLConfigFactoryTest {

    @Test
    public void test_create_ssl_configuration(@Mocked final Configuration sslConfiguration) {
        SSLConfigFactory sslConfigFactory = new SSLConfigFactory();

        new Expectations() {
            {
                sslConfiguration.getStringArray(CIPHERS);
                result = new String[]{"cipher1", "cipher2"};

                sslConfiguration.getString(PROTOCOL);
                result = "protocol1";

                sslConfiguration.getString(CLIENT_AUTH_MODE);
                result = "REQUESTED";
            }
        };

        SSLConfiguration configuration = sslConfigFactory.createSSLConfiguration(sslConfiguration);
        Assertions.assertThat(configuration.getCiphers()).containsOnly("cipher1", "cipher2");
        Assertions.assertThat(configuration.getProtocol()).isEqualTo("protocol1");
        Assertions.assertThat(configuration.getClientAuthMode()).isEqualTo(SSLAuthenticationMode.REQUESTED);
    }
}
