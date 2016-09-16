/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.configuration;

import org.seedstack.coffig.node.MapNode;
import org.seedstack.coffig.spi.ConfigurationComponent;
import org.seedstack.coffig.spi.ConfigurationProvider;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class PrioritizedProvider implements ConfigurationProvider {
    private final Map<String, PrioritizedConfigurationProvider> providers = new ConcurrentHashMap<>();
    private final AtomicBoolean dirty = new AtomicBoolean(true);

    @Override
    public MapNode provide() {
        return providers.entrySet().stream()
                .map(Map.Entry::getValue)
                .sorted(PrioritizedConfigurationProvider::compareTo)
                .map(PrioritizedConfigurationProvider::getConfigurationProvider)
                .map(ConfigurationProvider::provide)
                .reduce((conf1, conf2) -> (MapNode) conf1.merge(conf2))
                .orElse(new MapNode());
    }

    @Override
    public boolean isDirty() {
        return dirty.get() || providers.values().stream().map(PrioritizedConfigurationProvider::getConfigurationProvider).filter(ConfigurationComponent::isDirty).count() > 0;
    }

    @Override
    public PrioritizedProvider fork() {
        PrioritizedProvider fork = new PrioritizedProvider();
        for (Map.Entry<String, PrioritizedConfigurationProvider> providerEntry : providers.entrySet()) {
            fork.registerProvider(providerEntry.getKey(), (ConfigurationProvider) providerEntry.getValue().getConfigurationProvider().fork(), providerEntry.getValue().getPriority());
        }
        return fork;
    }

    public PrioritizedProvider registerProvider(String name, ConfigurationProvider configurationProvider) {
        return registerProvider(name, configurationProvider, 0);
    }

    public PrioritizedProvider registerProvider(String name, ConfigurationProvider configurationProvider, int priority) {
        if (providers.putIfAbsent(name, new PrioritizedConfigurationProvider(priority, configurationProvider)) != null) {
            throw new IllegalStateException("A provider already exists with name " + name);
        } else {
            dirty.set(true);
        }
        return this;
    }

    public PrioritizedProvider unregisterProvider(String name) {
        if (providers.remove(name) == null) {
            throw new IllegalStateException("No provider exists with name " + name);
        }
        return this;
    }

    public ConfigurationProvider getProvider(String name) {
        PrioritizedConfigurationProvider provider = providers.get(name);
        if (provider == null) {
            throw new IllegalStateException("No provider exists with name " + name);
        }
        return provider.configurationProvider;
    }

    private static class PrioritizedConfigurationProvider implements Comparable<PrioritizedConfigurationProvider> {
        private final int priority;
        private final ConfigurationProvider configurationProvider;

        private PrioritizedConfigurationProvider(int priority, ConfigurationProvider configurationProvider) {
            this.priority = priority;
            this.configurationProvider = configurationProvider;
        }

        @Override
        public int compareTo(PrioritizedConfigurationProvider o) {
            return Integer.compare(priority, o.priority);
        }

        int getPriority() {
            return priority;
        }

        ConfigurationProvider getConfigurationProvider() {
            return configurationProvider;
        }
    }
}
