/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.crypto.internal;

import org.apache.commons.configuration.Configuration;
import org.seedstack.seed.crypto.spi.SSLConfiguration;

/**
 * Constructs the SSL configuration based on the props files.
 *
 * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
 */
class SSLConfigFactory {

    public static final String PROTOCOL = "protocol";
    public static final String CIPHERS = "ciphers";
    public static final String CLIENT_AUTH_MODE = "client-auth-mode";

    /**
     * Extracts an {@link SSLConfiguration} from the Seed configuration.
     *
     * @return the SSL configuration
     */
    SSLConfiguration createSSLConfiguration(Configuration sslConfiguration) {
        String authenticationMode = sslConfiguration.getString(CLIENT_AUTH_MODE);
        String protocol = sslConfiguration.getString(PROTOCOL);
        String[] ciphers = sslConfiguration.getStringArray(CIPHERS);

        return new SSLConfiguration(protocol, authenticationMode, ciphers);
    }
}
