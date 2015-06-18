/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
/*
 * Creation : 12 juin 2015
 */
package org.seedstack.seed.core.internal.application;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.text.StrLookup;

/**
 * Registry to add {@link StrLookup} in the {@link Configuration}.
 * 
 * @author thierry.bouvet@mpsa.com
 */
public class ConfigurationLookupRegistry {

    private static ConfigurationLookupRegistry instance;

    private final Map<String, ConfigurationLookup> lookups = new ConcurrentHashMap<String, ConfigurationLookup>();

    /**
     * Create or return a new instance for this registry.
     * 
     * @return the registry instance
     */
    public static synchronized ConfigurationLookupRegistry getInstance() {
        if (instance == null) {
            instance = new ConfigurationLookupRegistry();
        }
        return instance;
    }

    /**
     * Register a new {@link ConfigurationLookup}. This
     * 
     * @param key the {@link StrLookup} key.
     * @param lookup the {@link ConfigurationLookup} which contains the {@link StrLookup} to use for this key.
     */
    public void register(String key, ConfigurationLookup lookup) {
        lookups.put(key, lookup);
    }

    /**
     * Map which contains the {@link StrLookup} key and the {@link ConfigurationLookup} to use for the corresponding key.
     * 
     * @return the map
     */
    public Map<String, ConfigurationLookup> getLookups() {
        return lookups;
    }

}
