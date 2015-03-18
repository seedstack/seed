/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.cache.internal;

import org.seedstack.seed.core.internal.application.ApplicationPlugin;
import io.nuun.kernel.api.Plugin;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.PluginException;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.core.AbstractPlugin;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.cache.Cache;
import javax.cache.Caching;
import javax.cache.configuration.Factory;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * This plugin provides JSR-107 cache integration.
 *
 * @author emmanuel.vinel@mpsa.com
 * @author adrien.lauer@mpsa.com
 */
public class CachePlugin extends AbstractPlugin {
    public static final String CACHE_PLUGIN_CONFIGURATION_PREFIX = "org.seedstack.seed.cache";
    private static final Logger LOGGER = LoggerFactory.getLogger(CachePlugin.class);

    private final Map<String, Cache> caches = new HashMap<String, Cache>();

    @Override
    public String name() {
        return "seed-cache-plugin";
    }

    @Override
    @SuppressWarnings("unchecked")
    public InitState init(InitContext initContext) {
        ApplicationPlugin confPlugin = (ApplicationPlugin) initContext.pluginsRequired().iterator().next();
        Configuration cachesConf = confPlugin.getApplication().getConfiguration().subset(CachePlugin.CACHE_PLUGIN_CONFIGURATION_PREFIX);

        String defaultProvider = cachesConf.getString("default-provider");
        String[] cacheNames = cachesConf.getStringArray("caches");

        if (defaultProvider != null) {
            LOGGER.info("Caching default provider is configured to {}", defaultProvider);
        } else {
            LOGGER.info("Caching default provider is not specified and will be autodetected from classpath");
        }

        if (cacheNames != null) {
            for (String cacheName : cacheNames) {
                Configuration cacheConf = cachesConf.subset("cache." + cacheName);
                MutableConfiguration cacheConfiguration = new MutableConfiguration();

                // Expiry policy
                String expiryPolicyFactory = cacheConf.getString("expiry-policy-factory");
                String expiryPolicy = cacheConf.getString("expiry-policy");
                Long expiryDuration = cacheConf.getLong("expiry-duration", 900);

                if (expiryPolicyFactory != null) {
                    try {
                        cacheConfiguration.setExpiryPolicyFactory((Factory) Class.forName(expiryPolicyFactory).newInstance());
                    } catch (Exception e) {
                        throw new PluginException("Unable to instantiate custom expiry policy factory " + expiryPolicyFactory, e);
                    }
                } else if (expiryPolicy != null && !expiryPolicy.isEmpty()) {
                    try {
                        cacheConfiguration.setExpiryPolicyFactory(BuiltinExpiryPolicy.valueOf(expiryPolicy.toUpperCase()).getFactory(new Duration(TimeUnit.SECONDS, expiryDuration)));
                    } catch (Exception e) {
                        throw new PluginException("Unable to instantiate built-in expiry policy " + expiryPolicy, e);
                    }
                }

                String providerClassname = cacheConf.getString("provider", defaultProvider);
                if (providerClassname == null) {
                    LOGGER.info("Configuring cache {} with autodetected provider", cacheName);
                    caches.put(cacheName, Caching.getCachingProvider().getCacheManager().createCache(cacheName, cacheConfiguration));
                } else {
                    LOGGER.info("Configuring cache {} with provider {}", cacheName, providerClassname);
                    caches.put(cacheName, Caching.getCachingProvider(providerClassname).getCacheManager().createCache(cacheName, cacheConfiguration));
                }
            }
        }

        return InitState.INITIALIZED;
    }

    @Override
    public void stop() {
        LOGGER.info("Destroying caches");
        Caching.getCachingProvider().close();
    }

    @Override
    public Collection<Class<? extends Plugin>> requiredPlugins() {
        Collection<Class<? extends Plugin>> plugins = new ArrayList<Class<? extends Plugin>>();
        plugins.add(ApplicationPlugin.class);
        return plugins;
    }

    @Override
    public Object nativeUnitModule() {
        return new CacheModule(this.caches);
    }
}
