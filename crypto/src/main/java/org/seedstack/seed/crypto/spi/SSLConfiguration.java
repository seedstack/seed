/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.crypto.spi;

import org.seedstack.seed.SeedException;
import org.seedstack.seed.crypto.internal.CryptoErrorCodes;

/**
 * This class contains the SSL configuration.
 *
 * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
 */
public class SSLConfiguration {

    public static final String DEFAULT_PROTOCOL = "TLS";
    public static final SSLAuthenticationMode DEFAULT_CLIENT_MODE = SSLAuthenticationMode.NOT_REQUESTED;
    private final String protocol;
    private final SSLAuthenticationMode clientAuthMode;
    private final String[] ciphers;

    /**
     * Constructs an SSL configuration.
     *
     * @param protocol       the protocol used
     * @param clientAuthMode the client authentication mode
     * @param ciphers        the ciphers used
     */
    public SSLConfiguration(String protocol, String clientAuthMode, String[] ciphers) {
        this.protocol = getProtocol(protocol);
        this.clientAuthMode = getSslAuthenticationMode(clientAuthMode);
        this.ciphers = ciphers;
    }

    private String getProtocol(String protocol) {
        String chosenProtocol;
        if (protocol != null && !"".equals(protocol)) {
            chosenProtocol = protocol;
        } else {
            chosenProtocol = DEFAULT_PROTOCOL;
        }
        return chosenProtocol;
    }

    private SSLAuthenticationMode getSslAuthenticationMode(String clientAuthMode) {
        SSLAuthenticationMode authenticationMode;
        if (clientAuthMode != null && !"".equals(clientAuthMode)) {
            try {
                authenticationMode = SSLAuthenticationMode.valueOf(clientAuthMode);
            } catch (IllegalArgumentException e) {
                throw SeedException.wrap(e, CryptoErrorCodes.INVALID_CLIENT_AUTHENTICATION_MODE).put("invalidMode", clientAuthMode);
            }
        } else {
            authenticationMode = DEFAULT_CLIENT_MODE;
        }
        return authenticationMode;
    }

    /**
     * @return the requested protocol
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     * @return the client authentication mode
     */
    public SSLAuthenticationMode getClientAuthMode() {
        return clientAuthMode;
    }

    /**
     * @return the ciphers used
     */
    public String[] getCiphers() {
        return ciphers;
    }

}
