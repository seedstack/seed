/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security;

import io.nuun.kernel.api.Plugin;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.core.AbstractPlugin;

import java.util.ArrayList;
import java.util.Collection;

import org.seedstack.seed.security.ldap.internal.LdapSecurityPlugin;

import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import com.unboundid.ldap.sdk.LDAPException;

public class LDAPITPlugin extends AbstractPlugin {

    private InMemoryDirectoryServer ds;

    @Override
    public String name() {
        return "LdapITPlugin";
    }

    @Override
    public Collection<Class<? extends Plugin>> dependentPlugins() {
        Collection<Class<? extends Plugin>> plugins = new ArrayList<Class<? extends Plugin>>();
        plugins.add(LdapSecurityPlugin.class);
        return plugins;
    }

    @Override
    public InitState init(InitContext initContext) {
        try {
            InMemoryDirectoryServerConfig config = new InMemoryDirectoryServerConfig("dc=example,dc=com");
            config.addAdditionalBindCredentials("cn=admin, ou=people, dc=example,dc=com", "admin");
            config.addAdditionalBindCredentials("cn=John Doe - 123456, ou=people, dc=example,dc=com", "password");
            InMemoryListenerConfig listenerConfig = new InMemoryListenerConfig("test", null, 53800, null, null, null);
            config.setListenerConfigs(listenerConfig);
            ds = new InMemoryDirectoryServer(config);
            ds.importFromLDIF(true, "src/it/resources/data.ldif");
            ds.startListening();
        } catch (LDAPException e) {
            throw new RuntimeException(e);
        }
        return InitState.INITIALIZED;
    }

    @Override
    public void stop() {
        if (ds != null)
            ds.shutDown(true);
    }

}
