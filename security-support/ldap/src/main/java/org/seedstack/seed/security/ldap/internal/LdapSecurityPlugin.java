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

import io.nuun.kernel.api.Plugin;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.core.AbstractPlugin;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.seedstack.seed.core.api.SeedException;
import org.seedstack.seed.core.internal.application.ApplicationPlugin;
import org.seedstack.seed.security.ldap.LDAPErrorCode;
import org.seedstack.seed.security.ldap.internal.realms.LDAPRealm;

import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPConnectionPool;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.ResultCode;

public class LdapSecurityPlugin extends AbstractPlugin {

    private static final String LDAP_CONFIG_PREFIX = "org.seedstack.seed.security.ldap";

    private static final String CHOSEN_REALMS = "org.seedstack.seed.security.realms";

    private static final String SERVER_HOST_PROP = "server-host";

    private static final String SERVER_PORT_PROP = "server-port";
    private static final int DEFAULT_SERVER_PORT = 389;

    private static final String NUM_CONNECTIONS_PROP = "num-connections";
    private static final int DEFAULT_NUM_CONNECTIONS = 10;

    private static final String ACCOUNT_DN_PROP = "account-dn";

    private static final String ACCOUNT_PASSWORD_PROP = "account-password";

    private LDAPConnectionPool ldapConnectionPool;

    private boolean startPlugin;

    @Override
    public String name() {
        return "LdapSecurityPlugin";
    }

    @Override
    public InitState init(InitContext initContext) {
        ApplicationPlugin appPlugin = (ApplicationPlugin) initContext.pluginsRequired().iterator().next();
        String[] realms = appPlugin.getApplication().getConfiguration().getStringArray(CHOSEN_REALMS);
        startPlugin = ArrayUtils.contains(realms, LDAPRealm.class.getSimpleName());

        if (startPlugin) {
            Configuration ldapConfiguration = appPlugin.getApplication().getConfiguration().subset(LDAP_CONFIG_PREFIX);
            // Initialize ldap pool connection
            String host = ldapConfiguration.getString(SERVER_HOST_PROP);
            if (host == null) {
                throw SeedException.createNew(LDAPErrorCode.NO_HOST_DEFINED).put("hostPropName", LDAP_CONFIG_PREFIX + "." + SERVER_HOST_PROP);
            }
            int port = ldapConfiguration.getInt(SERVER_PORT_PROP, DEFAULT_SERVER_PORT);
            int numConnections = ldapConfiguration.getInt(NUM_CONNECTIONS_PROP, DEFAULT_NUM_CONNECTIONS);
            String accountDn = StringUtils.join(ldapConfiguration.getStringArray(ACCOUNT_DN_PROP), ',');
            LDAPConnection connection;
            try {
                connection = new LDAPConnection(host, port, accountDn, ldapConfiguration.getString(ACCOUNT_PASSWORD_PROP));
                ldapConnectionPool = new LDAPConnectionPool(connection, numConnections);
            } catch (LDAPException e) {
                switch (e.getResultCode().intValue()) {
                case ResultCode.NO_SUCH_OBJECT_INT_VALUE:
                    throw SeedException.wrap(e, LDAPErrorCode.NO_SUCH_ACCOUNT).put("account", accountDn)
                            .put("propName", LDAP_CONFIG_PREFIX + "." + ACCOUNT_DN_PROP);
                case ResultCode.INVALID_CREDENTIALS_INT_VALUE:
                    throw SeedException.wrap(e, LDAPErrorCode.INVALID_CREDENTIALS).put("account", accountDn)
                            .put("passwordPropName", LDAP_CONFIG_PREFIX + "." + ACCOUNT_PASSWORD_PROP)
                            .put("userPropName", LDAP_CONFIG_PREFIX + "." + ACCOUNT_DN_PROP);
                case ResultCode.CONNECT_ERROR_INT_VALUE:
                    throw SeedException.wrap(e, LDAPErrorCode.CONNECT_ERROR).put("host", host).put("port", port)
                            .put("hostPropName", LDAP_CONFIG_PREFIX + "." + SERVER_HOST_PROP)
                            .put("portPropName", LDAP_CONFIG_PREFIX + "." + SERVER_PORT_PROP);
                default:
                    throw SeedException.wrap(e, LDAPErrorCode.LDAP_ERROR).put("message", e.getMessage()).put("host", host).put("port", port)
                            .put("account", accountDn);
                }
            }
        }
        return InitState.INITIALIZED;
    }

    @Override
    public Collection<Class<? extends Plugin>> requiredPlugins() {
        Collection<Class<? extends Plugin>> plugins = new ArrayList<Class<? extends Plugin>>();
        plugins.add(ApplicationPlugin.class);
        return plugins;
    }

    @Override
    public Object nativeUnitModule() {
        if (startPlugin)
            return new LdapSecurityModule(ldapConnectionPool);
        return null;
    }

    @Override
    public void stop() {
        if (ldapConnectionPool != null)
            ldapConnectionPool.close();
    }

}
