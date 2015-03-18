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

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import org.seedstack.seed.cache.spi.CacheConcern;
import org.jsr107.ri.annotations.guice.module.CacheAnnotationsModule;

import javax.cache.Cache;
import javax.cache.configuration.CompleteConfiguration;
import javax.cache.configuration.Factory;
import java.util.HashMap;
import java.util.Map;

@CacheConcern
class CacheModule extends AbstractModule {
    private Map<String, Cache> caches = new HashMap<String, Cache>();

    CacheModule(Map<String, Cache> caches) {
        this.caches = caches;
    }

    @Override
    protected void configure() {
        for (Map.Entry<String, Cache> cacheEntry : this.caches.entrySet()) {
            bind(Cache.class).annotatedWith(Names.named(cacheEntry.getKey())).toInstance(cacheEntry.getValue());

            Factory expiryPolicyFactory = ((CompleteConfiguration)cacheEntry.getValue().getConfiguration(CompleteConfiguration.class)).getExpiryPolicyFactory();
            if (expiryPolicyFactory != null) {
                requestInjection(expiryPolicyFactory);
            }

            Factory cacheLoaderFactory = ((CompleteConfiguration)cacheEntry.getValue().getConfiguration(CompleteConfiguration.class)).getCacheLoaderFactory();
            if (cacheLoaderFactory != null) {
                requestInjection(cacheLoaderFactory);
            }

            Factory cacheWriterFactory = ((CompleteConfiguration)cacheEntry.getValue().getConfiguration(CompleteConfiguration.class)).getCacheWriterFactory();
            if (cacheWriterFactory != null) {
                requestInjection(cacheWriterFactory);
            }
        }

        install(new CacheAnnotationsModule());
    }
}
