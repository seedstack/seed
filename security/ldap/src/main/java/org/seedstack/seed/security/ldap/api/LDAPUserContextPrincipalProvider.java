/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.ldap.api;

import org.seedstack.seed.security.api.principals.PrincipalProvider;

/**
 * A PrincipalProvider for a LDAPUserContext
 */
public class LDAPUserContextPrincipalProvider implements PrincipalProvider<LDAPUserContext> {

    private LDAPUserContext userContext;

    /**
     * Constructor with the LDAPuserContext
     * 
     * @param userContext the LDAPUserContext contained by this principal
     */
    public LDAPUserContextPrincipalProvider(LDAPUserContext userContext) {
        this.userContext = userContext;
    }

    /**
     * Returns the LDAPUserContext
     * 
     * @return the LDAPuserContext
     */
    @Override
    public LDAPUserContext getPrincipal() {
        return userContext;
    }

}
