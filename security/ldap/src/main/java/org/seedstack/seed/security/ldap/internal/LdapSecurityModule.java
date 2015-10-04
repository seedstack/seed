/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.ldap.internal;

import org.seedstack.seed.security.ldap.api.LDAPSupport;

import com.google.inject.AbstractModule;
import com.unboundid.ldap.sdk.LDAPConnectionPool;

public class LdapSecurityModule extends AbstractModule {

    private LDAPConnectionPool ldapConnectionPool;

    public LdapSecurityModule(LDAPConnectionPool ldapConnectionPool) {
        this.ldapConnectionPool = ldapConnectionPool;
    }

    @Override
    protected void configure() {
        bind(LDAPConnectionPool.class).toInstance(ldapConnectionPool);
        bind(LDAPSupport.class).to(DefaultLDAPSupport.class);
    }
}
