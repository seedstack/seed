/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.internal.init;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

class SeedProxyAuthenticator extends Authenticator {
    private final PasswordAuthentication httpsPasswordAuthentication;
    private final PasswordAuthentication httpPasswordAuthentication;

    public SeedProxyAuthenticator(PasswordAuthentication httpPasswordAuthentication,
            PasswordAuthentication httpsPasswordAuthentication) {
        this.httpPasswordAuthentication = httpPasswordAuthentication;
        this.httpsPasswordAuthentication = httpsPasswordAuthentication;
    }

    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
        if ("http".equalsIgnoreCase(getRequestingProtocol())) {
            return httpPasswordAuthentication;
        } else if ("https".equalsIgnoreCase(getRequestingProtocol())) {
            return httpsPasswordAuthentication;
        } else {
            return null;
        }
    }
}