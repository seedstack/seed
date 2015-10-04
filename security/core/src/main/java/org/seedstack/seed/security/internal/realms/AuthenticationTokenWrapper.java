/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.internal.realms;

import org.apache.shiro.authc.AuthenticationToken;

/**
 * A Shiro authentication token that wrap a Seed authentication token
 * 
 * @author yves.dautremay@mpsa.com
 * 
 */
public class AuthenticationTokenWrapper implements AuthenticationToken {

    private static final long serialVersionUID = 3333761741487888393L;

    private org.seedstack.seed.security.api.AuthenticationToken seedToken;

    /**
     * Constructor with the Seed token
     * 
     * @param seedToken
     *            the seed token to wrap
     */
    public AuthenticationTokenWrapper(org.seedstack.seed.security.api.AuthenticationToken seedToken) {
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

    /**
     * Gives the seed authentication token
     * 
     * @return the seed authentication token
     */
    public org.seedstack.seed.security.api.AuthenticationToken getSeedToken() {
        return seedToken;
    }
}
