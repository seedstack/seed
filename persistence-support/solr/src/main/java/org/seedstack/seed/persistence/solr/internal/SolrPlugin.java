/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
/**
 *
 */
package org.seedstack.seed.persistence.solr.internal;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.nuun.kernel.api.Plugin;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.PluginException;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.core.AbstractPlugin;
import org.apache.commons.configuration.Configuration;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.impl.LBHttpSolrClient;
import org.seedstack.seed.core.api.Application;
import org.seedstack.seed.core.api.SeedException;
import org.seedstack.seed.core.internal.application.ApplicationPlugin;
import org.seedstack.seed.persistence.solr.api.SolrErrorCodes;
import org.seedstack.seed.persistence.solr.api.SolrExceptionHandler;
import org.seedstack.seed.transaction.internal.TransactionPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * This plugin manages configured Solr clients.
 *
 * @author redouane.loulou@ext.mpsa.com
 */
public class SolrPlugin extends AbstractPlugin {
    public static final String SOLR_PLUGIN_CONFIGURATION_PREFIX = "org.seedstack.seed.persistence.solr";
    private static final Logger LOGGER = LoggerFactory.getLogger(SolrPlugin.class);

    private final Map<String, SolrClient> solrClients = new HashMap<String, SolrClient>();
    private final Map<String, Class<? extends SolrExceptionHandler>> solrTransactionHandlers = new HashMap<String, Class<? extends SolrExceptionHandler>>();

    @Override
    public String name() {
        return "seed-solr-plugin";
    }

    @Override
    @SuppressWarnings("unchecked")
    public InitState init(InitContext initContext) {
        Application application = null;
        TransactionPlugin transactionPlugin = null;
        Configuration solrConfiguration = null;

        for (Plugin plugin : initContext.pluginsRequired()) {
            if (plugin instanceof ApplicationPlugin) {
                application = ((ApplicationPlugin) plugin).getApplication();
                solrConfiguration = application.getConfiguration().subset(SolrPlugin.SOLR_PLUGIN_CONFIGURATION_PREFIX);
            } else if (plugin instanceof TransactionPlugin) {
                transactionPlugin = (TransactionPlugin) plugin;
            }
        }

        if (application == null) {
            throw new PluginException("Unable to find application plugin");
        }
        if (transactionPlugin == null) {
            throw new PluginException("Unable to find transaction plugin");
        }

        String[] solrClients = solrConfiguration.getStringArray("clients");

        if (solrClients == null || solrClients.length == 0) {
            LOGGER.info("No Solr client configured, Solr support disabled");
            return InitState.INITIALIZED;
        }

        for (String solrClient : solrClients) {
            Configuration solrClientConfiguration = solrConfiguration.subset("client." + solrClient);

            try {
                this.solrClients.put(solrClient, buildSolrClient(solrClientConfiguration));
            } catch (Exception e) {
                throw SeedException.wrap(e, SolrErrorCodes.UNABLE_TO_CREATE_CLIENT).put("clientName", solrClient);
            }

            String exceptionHandler = solrClientConfiguration.getString("exception-handler");
            if (exceptionHandler != null && !exceptionHandler.isEmpty()) {
                try {
                    solrTransactionHandlers.put(solrClient, (Class<? extends SolrExceptionHandler>) Class.forName(exceptionHandler));
                } catch (Exception e) {
                    throw new PluginException("Unable to load class " + exceptionHandler, e);
                }
            }
        }

        if (solrClients.length == 1) {
            SolrTransactionMetadataResolver.defaultSolrClient = solrClients[0];
        }

        transactionPlugin.registerTransactionHandler(SolrTransactionHandler.class);

        return InitState.INITIALIZED;
    }

    @Override
    public void stop() {
        for (Map.Entry<String, SolrClient> solrClientEntry : solrClients.entrySet()) {
            LOGGER.info("Closing Solr client {}", solrClientEntry.getKey());
            try {
                solrClientEntry.getValue().close();
            } catch (Exception e) {
                LOGGER.error(String.format("Unable to properly close Solr client %s", solrClientEntry.getKey()), e);
            }
        }
    }

    @Override
    public Collection<Class<? extends Plugin>> requiredPlugins() {
        Collection<Class<? extends Plugin>> plugins = new ArrayList<Class<? extends Plugin>>();
        plugins.add(ApplicationPlugin.class);
        plugins.add(TransactionPlugin.class);
        return plugins;
    }

    @Override
    public Object nativeUnitModule() {
        return new SolrModule(solrClients, solrTransactionHandlers);
    }

    private SolrClient buildSolrClient(Configuration solrClientConfiguration) throws MalformedURLException {
        SolrClientType clientType = SolrClientType.valueOf(solrClientConfiguration.getString(SolrConfigurationConstants.TYPE, SolrClientType.HTTP.toString()));
        String[] urls = solrClientConfiguration.getStringArray(SolrConfigurationConstants.URLS);

        if (urls == null || urls.length == 0 || urls[0].isEmpty()) {
            urls = new String[]{solrClientConfiguration.getString(SolrConfigurationConstants.URL)};
            if (urls[0] == null || urls[0].isEmpty()) {
                throw SeedException.createNew(SolrErrorCodes.MISSING_URL_CONFIGURATION);
            }
        }

        switch (clientType) {
            case LOAD_BALANCED_HTTP:
                return buildLBSolrClient(solrClientConfiguration, urls);
            case HTTP:
                return buildHttpSolrClient(solrClientConfiguration, urls[0]);
            case CLOUD:
                return buildCloudSolrClient(solrClientConfiguration, urls);
            default:
                throw SeedException.createNew(SolrErrorCodes.UNSUPPORTED_CLIENT_TYPE);
        }
    }

    private SolrClient buildLBSolrClient(Configuration solrClientConfiguration, String[] lbUrls) throws MalformedURLException {
        LBHttpSolrClient lbHttpSolrClient = new LBHttpSolrClient(lbUrls);

        if (solrClientConfiguration.containsKey(SolrConfigurationConstants.CONNECTION_TIMEOUT)) {
            lbHttpSolrClient.setConnectionTimeout(solrClientConfiguration.getInt(SolrConfigurationConstants.CONNECTION_TIMEOUT));
        }

        if (solrClientConfiguration.containsKey(SolrConfigurationConstants.ALIVE_CHECK_INTERVAL)) {
            lbHttpSolrClient.setAliveCheckInterval(solrClientConfiguration.getInt(SolrConfigurationConstants.ALIVE_CHECK_INTERVAL));
        }

        if (solrClientConfiguration.containsKey(SolrConfigurationConstants.QUERY_PARAMS)) {
            lbHttpSolrClient.setQueryParams(Sets.newHashSet(solrClientConfiguration.getStringArray(SolrConfigurationConstants.QUERY_PARAMS)));
        }

        if (solrClientConfiguration.containsKey(SolrConfigurationConstants.SO_TIMEOUT)) {
            lbHttpSolrClient.setSoTimeout(solrClientConfiguration.getInt(SolrConfigurationConstants.SO_TIMEOUT));
        }

        return lbHttpSolrClient;
    }

    private SolrClient buildHttpSolrClient(Configuration solrClientConfiguration, String url) {
        HttpSolrClient httpSolrClient = new HttpSolrClient(url);

        if (solrClientConfiguration.containsKey(SolrConfigurationConstants.CONNECTION_TIMEOUT)) {
            httpSolrClient.setConnectionTimeout(solrClientConfiguration.getInt(SolrConfigurationConstants.CONNECTION_TIMEOUT));
        }

        if (solrClientConfiguration.containsKey(SolrConfigurationConstants.QUERY_PARAMS)) {
            httpSolrClient.setQueryParams(Sets.newHashSet(solrClientConfiguration.getStringArray(SolrConfigurationConstants.QUERY_PARAMS)));
        }

        if (solrClientConfiguration.containsKey(SolrConfigurationConstants.SO_TIMEOUT)) {
            httpSolrClient.setSoTimeout(solrClientConfiguration.getInt(SolrConfigurationConstants.SO_TIMEOUT));
        }

        if (solrClientConfiguration.containsKey(SolrConfigurationConstants.ALLOW_COMPRESSION)) {
            httpSolrClient.setAllowCompression(solrClientConfiguration.getBoolean(SolrConfigurationConstants.ALLOW_COMPRESSION));
        }

        if (solrClientConfiguration.containsKey(SolrConfigurationConstants.MAX_CONNECTIONS_PER_HOST)) {
            httpSolrClient.setDefaultMaxConnectionsPerHost(solrClientConfiguration.getInt(SolrConfigurationConstants.MAX_CONNECTIONS_PER_HOST));
        }

        if (solrClientConfiguration.containsKey(SolrConfigurationConstants.FOLLOW_REDIRECTS)) {
            httpSolrClient.setFollowRedirects(solrClientConfiguration.getBoolean(SolrConfigurationConstants.FOLLOW_REDIRECTS));
        }

        if (solrClientConfiguration.containsKey(SolrConfigurationConstants.MAX_TOTAL_CONNECTIONS)) {
            httpSolrClient.setMaxTotalConnections(solrClientConfiguration.getInt(SolrConfigurationConstants.MAX_TOTAL_CONNECTIONS));
        }

        if (solrClientConfiguration.containsKey(SolrConfigurationConstants.USE_MULTI_PART_HOST)) {
            httpSolrClient.setUseMultiPartPost(solrClientConfiguration.getBoolean(SolrConfigurationConstants.USE_MULTI_PART_HOST));
        }

        return httpSolrClient;
    }

    private CloudSolrClient buildCloudSolrClient(Configuration solrClientConfiguration, String[] zooKeeperUrls) throws MalformedURLException {
        String[] lbUrls = solrClientConfiguration.getStringArray(SolrConfigurationConstants.LB_URLS);

        CloudSolrClient cloudSolrClient;
        if (lbUrls != null && lbUrls.length > 0) {
            cloudSolrClient = new CloudSolrClient(zooKeeperUrls[0], new LBHttpSolrClient(lbUrls), solrClientConfiguration.getBoolean(SolrConfigurationConstants.UPDATE_TO_LEADERS, true));
        } else {
            cloudSolrClient = new CloudSolrClient(Lists.newArrayList(zooKeeperUrls), solrClientConfiguration.getString(SolrConfigurationConstants.CHROOT));
        }

        String defaultCollection = solrClientConfiguration.getString(SolrConfigurationConstants.DEFAULT_COLLECTION);
        if (defaultCollection != null && !defaultCollection.isEmpty()) {
            cloudSolrClient.setDefaultCollection(defaultCollection);
        }

        String idField = solrClientConfiguration.getString(SolrConfigurationConstants.ID_FIELD);
        if (idField != null && !idField.isEmpty()) {
            cloudSolrClient.setIdField(idField);
        }

        if (solrClientConfiguration.containsKey(SolrConfigurationConstants.COLLECTION_CACHE_TTL)) {
            cloudSolrClient.setCollectionCacheTTl(solrClientConfiguration.getInt(SolrConfigurationConstants.COLLECTION_CACHE_TTL));
        }

        if (solrClientConfiguration.containsKey(SolrConfigurationConstants.PARALLEL_CACHE_REFRESHES)) {
            cloudSolrClient.setParallelCacheRefreshes(solrClientConfiguration.getInt(SolrConfigurationConstants.PARALLEL_CACHE_REFRESHES));
        }

        if (solrClientConfiguration.containsKey(SolrConfigurationConstants.PARALLEL_UPDATES)) {
            cloudSolrClient.setParallelUpdates(solrClientConfiguration.getBoolean(SolrConfigurationConstants.PARALLEL_UPDATES));
        }

        if (solrClientConfiguration.containsKey(SolrConfigurationConstants.ZK_CLIENT_TIMEOUT)) {
            cloudSolrClient.setZkClientTimeout(solrClientConfiguration.getInt(SolrConfigurationConstants.ZK_CLIENT_TIMEOUT));
        }

        if (solrClientConfiguration.containsKey(SolrConfigurationConstants.ZK_CONNECT_TIMEOUT)) {
            cloudSolrClient.setZkConnectTimeout(solrClientConfiguration.getInt(SolrConfigurationConstants.ZK_CONNECT_TIMEOUT));
        }
        return cloudSolrClient;
    }
}
