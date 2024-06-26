/*
 * Copyright © 2013-2024, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.internal.realms;

import org.apache.shiro.authc.AuthenticationToken;

/**
 * A Shiro authentication token that wrap a SeedStack authentication token.
 */
public class AuthenticationTokenWrapper implements AuthenticationToken {
    private static final long serialVersionUID = 1L;
    private org.seedstack.seed.security.AuthenticationToken seedToken;

    /**
     * Constructor with the SeedStack token.
     *
     * @param seedToken the seed token to wrap.
     */
    public AuthenticationTokenWrapper(org.seedstack.seed.security.AuthenticationToken seedToken) {
        this.seedToken = seedToken;
    }

    @Override
    public Object getPrincipal() {
        return seedToken.getPrincipal();
    }

    @Override
    public Object getCredentials() {
        return seedToken.getCredentials();
    }

    @Override
    public String toString() {
        return seedToken.name();
    }

    /**
     * Gives the seed authentication token.
     *
     * @return the seed authentication token.
     */
    public org.seedstack.seed.security.AuthenticationToken getSeedToken() {
        return seedToken;
    }
}
