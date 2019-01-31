/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security;

import java.util.ArrayList;
import java.util.Collection;
import org.seedstack.seed.security.principals.PrincipalProvider;
import org.seedstack.seed.security.principals.Principals;

/**
 * Information about an authentication : the principals and credentials.
 */
public class AuthenticationInfo {

    private PrincipalProvider<?> identityPrincipal;

    private Collection<PrincipalProvider<?>> otherPrincipals = new ArrayList<>();

    private Object credentials;

    /**
     * Constructor using Principal as identity principal.
     *
     * @param identityPrincipal identityPrincipal
     * @param credentials       credentials
     */
    public AuthenticationInfo(PrincipalProvider<?> identityPrincipal, Object credentials) {
        this.identityPrincipal = identityPrincipal;
        this.credentials = credentials;
    }

    /**
     * Constructor using String. Creates a SimplePrincipal with the id.
     *
     * @param id          id
     * @param credentials credentials
     */
    public AuthenticationInfo(String id, Object credentials) {
        this.identityPrincipal = Principals.identityPrincipal(id);
        this.credentials = credentials;
    }

    /**
     * Getter identityPrincipal
     *
     * @return the identityPrincipal
     */
    public PrincipalProvider<?> getIdentityPrincipal() {
        return identityPrincipal;
    }

    /**
     * Getter otherPrincipals
     *
     * @return the otherPrincipals
     */
    public Collection<PrincipalProvider<?>> getOtherPrincipals() {
        return otherPrincipals;
    }

    /**
     * Getter credentials
     *
     * @return the credentials
     */
    public Object getCredentials() {
        return credentials;
    }
}
