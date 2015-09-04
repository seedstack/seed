/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.persistence.elasticsearch.internal;

import io.nuun.kernel.api.Plugin;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.Context;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.core.AbstractPlugin;
import org.apache.commons.configuration.Configuration;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.seedstack.seed.core.api.SeedException;
import org.seedstack.seed.core.internal.application.ApplicationPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This plugin manages clients used to access ElasticSearch instances.
 *
 * @author redouane.loulou@ext.mpsa.com
 */
public class ElasticSearchPlugin extends AbstractPlugin {
    public static final String ELASTIC_SEARCH_PLUGIN_CONFIGURATION_PREFIX = "org.seedstack.seed.persistence.elasticsearch";
    public static final int DEFAULT_ELASTIC_SEARCH_PORT = 9300;
    public static final String ELASTIC_SEARCH_STORAGE_ROOT = "persistence-elasticsearch" + File.separator;
    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchPlugin.class);

    private final Map<String, Client> elasticSearchClients = new HashMap<String, Client>();
    private final Map<String, Node> elasticSearchLocalNodes = new HashMap<String, Node>();

    @Override
    public String name() {
        return "seed-elasticsearch-plugin";
    }

    @Override
    public InitState init(InitContext initContext) {
        ApplicationPlugin applicationPlugin = (ApplicationPlugin) initContext.pluginsRequired().iterator().next();
        Configuration elasticSearchConfiguration = applicationPlugin.getApplication().getConfiguration().subset(ELASTIC_SEARCH_PLUGIN_CONFIGURATION_PREFIX);

        String[] elasticSearchClientNames = elasticSearchConfiguration.getStringArray("clients");
        if (elasticSearchClientNames != null && elasticSearchClientNames.length > 0) {
            for (String elasticSearchClientName : elasticSearchClientNames) {
                Configuration elasticSearchClientConfiguration = elasticSearchConfiguration.subset("client." + elasticSearchClientName);

                Iterator<String> it = elasticSearchClientConfiguration.getKeys("property");
                Map<String, String> propertiesMap = new HashMap<String, String>();
                while (it.hasNext()) {
                    String name = it.next();
                    propertiesMap.put(name.substring(9), elasticSearchClientConfiguration.getString(name));
                }

                if (!propertiesMap.containsKey("path.home")) {
                    propertiesMap.put("path.home", applicationPlugin.getApplication().getStorageLocation(ElasticSearchPlugin.ELASTIC_SEARCH_STORAGE_ROOT + elasticSearchClientName).getAbsolutePath());
                }

                String[] hosts = elasticSearchClientConfiguration.getStringArray("hosts");
                if (hosts == null || hosts.length == 0) {
                    LOGGER.info("Creating ElasticSearch client {} on its local node", elasticSearchClientName);

                    Node node = buildLocalNode(buildSettings(propertiesMap));
                    elasticSearchLocalNodes.put(elasticSearchClientName, node);
                    elasticSearchClients.put(elasticSearchClientName, node.client());
                } else {
                    LOGGER.info("Creating ElasticSearch client {} for remote instance at {}", elasticSearchClientName, Arrays.toString(hosts));

                    elasticSearchClients.put(elasticSearchClientName, buildRemoteClient(buildSettings(propertiesMap), hosts));
                }
            }

        } else {
            LOGGER.info("No ElasticSearch client configured, ElasticSearch support disabled");
        }

        return InitState.INITIALIZED;
    }

    @Override
    public void start(Context context) {
        for (Entry<String, Node> entry : elasticSearchLocalNodes.entrySet()) {
            LOGGER.info("Starting ElasticSearch local node {}", entry.getKey());
            entry.getValue().start();
        }
    }

    @Override
    public void stop() {
        for (Entry<String, Client> entry : elasticSearchClients.entrySet()) {
            LOGGER.info("Closing ElasticSearch client {}", entry.getKey());
            try {
                entry.getValue().close();
            } catch (Exception e) {
                LOGGER.error(String.format("Unable to properly close ElasticSearch client %s", entry.getKey()), e);
            }
        }

        for (Entry<String, Node> entry : elasticSearchLocalNodes.entrySet()) {
            LOGGER.info("Closing ElasticSearch local node {}", entry.getKey());
            try {
                entry.getValue().close();
            } catch (Exception e) {
                LOGGER.error(String.format("Unable to properly close ElasticSearch local node %s", entry.getKey()), e);
            }
        }
    }

    @Override
    public Collection<Class<? extends Plugin>> requiredPlugins() {
        Collection<Class<? extends Plugin>> plugins = new ArrayList<Class<? extends Plugin>>();
        plugins.add(ApplicationPlugin.class);
        return plugins;
    }

    @Override
    public Object nativeUnitModule() {
        return new ElasticSearchModule(elasticSearchClients);
    }

    private Node buildLocalNode(Settings settings) {
        NodeBuilder nodeBuilder = NodeBuilder.nodeBuilder();
        nodeBuilder.settings(settings);
        nodeBuilder.local(true);
        nodeBuilder.loadConfigSettings(false);

        return nodeBuilder.node();
    }

    private Client buildRemoteClient(Settings settings, String[] hosts) {
        TransportClient transportClient = new TransportClient(settings, false);

        for (String host : hosts) {
            String[] hostInfo = host.split(":");
            if (hostInfo.length > 2) {
                throw SeedException.createNew(ElasticSearchErrorCode.INVALID_HOST).put("host", host);
            }
            String address = hostInfo[0].trim();
            int port = DEFAULT_ELASTIC_SEARCH_PORT;
            try {
                if (hostInfo.length > 1) {
                    port = Integer.valueOf(hostInfo[1]);
                }
            } catch (NumberFormatException e) {
                throw SeedException.wrap(e, ElasticSearchErrorCode.CLIENT_INVALID_PORT).put("host", hostInfo[0]);
            }

            transportClient.addTransportAddress(new InetSocketTransportAddress(address, port));
        }

        return transportClient;
    }

    private Settings buildSettings(Map<String, String> settings) {
        ImmutableSettings.Builder settingsBuilder = ImmutableSettings.settingsBuilder();
        settingsBuilder.put(settings);
        return settingsBuilder.build();
    }
}
