/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.ws.internal.jms;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.collect.ImmutableList;
import io.nuun.kernel.api.Plugin;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.PluginException;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.core.AbstractPlugin;
import org.apache.commons.configuration.Configuration;
import org.seedstack.seed.core.internal.application.ApplicationPlugin;
import org.seedstack.seed.jms.internal.JmsPlugin;
import org.seedstack.seed.jms.spi.MessageListenerDefinition;
import org.seedstack.seed.ws.internal.EndpointDefinition;
import org.seedstack.seed.ws.internal.WSPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.naming.NamingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This plugin provides JMS transport integration for WS support.
 *
 * @author adrien.lauer@mpsa.com
 */
public class WSJmsPlugin extends AbstractPlugin {
    public static final List<String> SUPPORTED_BINDINGS = ImmutableList.of("http://www.w3.org/2010/soapjms/");
    public static final String WS_CONFIGURATION_PREFIX = "org.seedstack.seed.ws";

    private static final Logger LOGGER = LoggerFactory.getLogger(WSJmsPlugin.class);
    private static final int DEFAULT_CACHE_CONCURRENCY = 4;
    private static final int DEFAULT_CACHE_SIZE = 16;

    private final Set<WSJmsMessageListener> wsJmsMessageListeners = new HashSet<WSJmsMessageListener>();

    private LoadingCache<SoapJmsUri, Connection> connectionCache;

    private JmsPlugin jmsPlugin;
    private WSPlugin wsPlugin;

    @Override
    public String name() {
        return "seed-ws-jms-plugin";
    }

    @Override
    public InitState init(InitContext initContext) {
        Configuration wsConfiguration = null;

        for (Plugin plugin : initContext.pluginsRequired()) {
            if (plugin instanceof JmsPlugin) {
                jmsPlugin = (JmsPlugin) plugin;
            }

            if (plugin instanceof WSPlugin) {
                wsPlugin = (WSPlugin) plugin;
            }

            if (plugin instanceof ApplicationPlugin) {
                wsConfiguration = ((ApplicationPlugin) plugin).getApplication().getConfiguration().subset(WS_CONFIGURATION_PREFIX);
            }
        }

        if (wsConfiguration == null) {
            throw new PluginException("Missing required application plugin");
        }

        int cacheSize = wsConfiguration.getInt("jms.transport-cache.max-size", DEFAULT_CACHE_SIZE);
        connectionCache = CacheBuilder.newBuilder()
                .maximumSize(cacheSize)
                .concurrencyLevel(wsConfiguration.getInt("transport-cache.concurrency", DEFAULT_CACHE_CONCURRENCY))
                .initialCapacity(wsConfiguration.getInt("transport-cache.initial-size", cacheSize / 4))
                .removalListener(new RemovalListener<SoapJmsUri, Connection>() {
                    @Override
                    public void onRemoval(RemovalNotification<SoapJmsUri, Connection> removal) {
                        Connection connection = removal.getValue();
                        SoapJmsUri soapJmsUri = removal.getKey();
                        try {
                            if (connection != null && soapJmsUri != null && soapJmsUri.getParameter("connectionName") == null) {
                                // Don't close connections retrieved from the jms plugin
                                connection.close();
                            }
                        } catch (JMSException e) {
                            LOGGER.warn("Unable to cleanup cached JMS transport", e);
                        }
                    }
                })
                .build(new CacheLoader<SoapJmsUri, Connection>() {
                    @Override
                    public Connection load(SoapJmsUri soapJmsUri) throws NamingException, JMSException {
                        Connection connection = getConnection(soapJmsUri, null, null);
                        connection.start();
                        return connection;
                    }
                });

        for (Map.Entry<String, EndpointDefinition> endpointEntry : wsPlugin.getEndpointDefinitions(SUPPORTED_BINDINGS).entrySet()) {
            EndpointDefinition endpointDefinition = endpointEntry.getValue();
            String endpointName = endpointEntry.getKey();
            String serviceName = endpointDefinition.getServiceName().getLocalPart();
            String portName = endpointDefinition.getPortName().getLocalPart();
            String serviceNameAndServicePort = serviceName + "-" + portName;

            SoapJmsUri uri;
            try {
                uri = SoapJmsUri.parse(new URI(endpointDefinition.getUrl()));
            } catch (URISyntaxException e) {
                throw new PluginException("Unable to parse endpoint URI", e);
            }

            Connection connection;
            String jmsName = "WS-" + endpointName;
            try {
                connection = getConnection(uri, endpointDefinition.getConfiguration().subset("jms"), jmsName);
                jmsPlugin.registerConnection(jmsName, connection);
            } catch (Exception e) {
                throw new PluginException("Unable to create JMS connection for WS " + serviceNameAndServicePort, e);
            }

            Session session;
            try {
                session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
            } catch (JMSException e) {
                throw new PluginException("Unable to create JMS session for WS " + serviceNameAndServicePort, e);
            }

            Destination destination;
            try {
                destination = SoapJmsUri.getDestination(uri, session);
            } catch (Exception e) {
                throw new PluginException("Unable to create JMS destination for WS " + serviceNameAndServicePort, e);
            }

            WSJmsMessageListener messageListener = new WSJmsMessageListener(session, uri,
                    new JmsAdapter(wsPlugin.createWSEndpoint(endpointDefinition, null)));

            jmsPlugin.registerMessageListener(jmsName, new MessageListenerDefinition(
                    messageListener, session, destination, null));

            wsJmsMessageListeners.add(messageListener);
        }

        return InitState.INITIALIZED;
    }

    @Override
    public void stop() {
        if (connectionCache != null) {
            connectionCache.invalidateAll();
            connectionCache.cleanUp();
        }
    }

    @Override
    public Collection<Class<? extends Plugin>> requiredPlugins() {
        Collection<Class<? extends Plugin>> plugins = new ArrayList<Class<? extends Plugin>>();
        plugins.add(WSPlugin.class);
        plugins.add(ApplicationPlugin.class);
        plugins.add(JmsPlugin.class);

        return plugins;
    }

    @Override
    public Object nativeUnitModule() {
        return new WSJmsModule(this, wsJmsMessageListeners, connectionCache);
    }

    private Connection getConnection(SoapJmsUri soapJmsUri, Configuration jmsConfiguration, String connectionName) throws NamingException, JMSException {
        Connection connection;

        String lookupVariant = soapJmsUri.getLookupVariant();
        if (SoapJmsUri.JNDI_LOOKUP_VARIANT.equals(lookupVariant)) {
            connection = jmsPlugin.createConnection(
                    (ConnectionFactory) SoapJmsUri.getContext(soapJmsUri).lookup(soapJmsUri.getParameter("jndiConnectionFactoryName")),
                    jmsPlugin.createConnectionDefinitionFromConfiguration(jmsConfiguration),
                    connectionName
            );
        } else if (SoapJmsUri.SEED_QUEUE_LOOKUP_VARIANT.equals(lookupVariant) || SoapJmsUri.SEED_TOPIC_LOOKUP_VARIANT.equals(lookupVariant)) {
            connection = jmsPlugin.getConnection(soapJmsUri.getParameter("connectionName"));
        } else {
            throw new IllegalArgumentException("Unsupported lookup variant " + lookupVariant + " for JMS URI " + soapJmsUri.toString());
        }

        if (connection == null) {
            throw new PluginException("Unable to resolve connection for JMS URI " + soapJmsUri.toString());
        }

        return connection;
    }
}
