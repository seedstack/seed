/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.crypto.internal;

import org.apache.commons.configuration.Configuration;

/**
 * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
 */
public class SslConfigFactory {

    public static final String SSL = "ssl";
    public static final String PROTOCOL = "protocol";
    public static final String CIPHERS = "ciphers";
    public static final String CLIENT_AUTH_MODE = "client-auth-mode";
    public static final String NOT_REQUESTED = "NOT_REQUESTED";
    public static final String TLS = "TLS";

    /**
     * Extract an {@link SslConfig} from the Seed configuration.
     *
     * @return the SSL configuration
     */
    SslConfig createSslConfig(Configuration sslConfiguration) {
        SslConfig sslConfig = new SslConfig();

        String sslClientAuthMode = sslConfiguration.getString(CLIENT_AUTH_MODE);
        if (sslClientAuthMode != null && !sslClientAuthMode.equals("") && SslConfig.CLIENT_AUTH_MODES.contains(sslClientAuthMode)) {
            sslConfig.setClientAuthMode(sslClientAuthMode);
        } else {
            sslConfig.setClientAuthMode(NOT_REQUESTED);
        }

        if (sslConfiguration.containsKey(PROTOCOL)) {
            sslConfig.setProtocol(sslConfiguration.getString(PROTOCOL));
        } else {
            sslConfig.setProtocol(TLS);
        }
        sslConfig.setCiphers(sslConfiguration.getStringArray(CIPHERS));

        return sslConfig;
    }
}
