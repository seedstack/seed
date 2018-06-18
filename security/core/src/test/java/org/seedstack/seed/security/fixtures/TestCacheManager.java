/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.fixtures;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import org.apache.shiro.cache.AbstractCacheManager;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.apache.shiro.cache.MapCache;
import org.seedstack.seed.Logging;
import org.slf4j.Logger;

public class TestCacheManager extends AbstractCacheManager {
    @Logging
    private Logger logger;

    @Override
    @SuppressWarnings("unchecked")
    protected Cache createCache(String s) throws CacheException {
        return new TestCache(s, new HashMap());
    }

    private class TestCache<K, V> extends MapCache<K, V> {
        private final String name;
        private Class<?> keyType;

        TestCache(String name, Map<K, V> backingMap) {
            super(name, backingMap);
            this.name = name;
        }

        @Override
        public V get(K key) throws CacheException {
            logger.info("Getting {} from {} security cache", key, name);
            assertKeyClass(key);
            return super.get(key);
        }

        @Override
        public V put(K key, V value) throws CacheException {
            logger.info("Putting {} / {} in {} security cache", key, value, name);
            assertKeyClass(key);
            return super.put(key, value);
        }

        @Override
        public V remove(K key) throws CacheException {
            logger.info("Removing {} from {} security cache", key, name);
            assertKeyClass(key);
            return super.remove(key);
        }

        private synchronized void assertKeyClass(K key) {
            if (keyType == null) {
                keyType = key.getClass();
            } else {
                assertThat(key).isOfAnyClassIn(keyType);
            }
        }
    }
}
