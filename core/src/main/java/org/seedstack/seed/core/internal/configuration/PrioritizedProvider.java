/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.configuration;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import org.seedstack.coffig.Coffig;
import org.seedstack.coffig.node.MapNode;
import org.seedstack.coffig.spi.ConfigurationComponent;
import org.seedstack.coffig.spi.ConfigurationProvider;
import org.seedstack.coffig.spi.ConfigurationWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrioritizedProvider implements ConfigurationProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(PrioritizedProvider.class);
    private final List<PrioritizedConfigurationProvider> providers = new CopyOnWriteArrayList<>();
    private final AtomicBoolean dirty = new AtomicBoolean(true);

    @Override
    public MapNode provide() {
        MapNode mapNode = providers.stream()
                .sorted(Comparator.comparingInt(o -> o.priority))
                .map(prioritizedConfigurationProvider -> {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Fetching configuration from provider {} at priority {}",
                                prioritizedConfigurationProvider.getConfigurationProvider().name(),
                                prioritizedConfigurationProvider.getPriority());
                    }
                    return prioritizedConfigurationProvider.getConfigurationProvider();
                })
                .map(ConfigurationProvider::provide)
                .reduce((conf1, conf2) -> (MapNode) conf1.merge(conf2))
                .orElse(new MapNode());
        dirty.set(false);
        LOGGER.debug("Configuration fetching complete");
        return mapNode;
    }

    @Override
    public void initialize(Coffig coffig) {
        providers.stream().map(PrioritizedConfigurationProvider::getConfigurationProvider)
                .forEach(provider -> provider.initialize(coffig));
    }

    @Override
    public boolean isDirty() {
        return dirty.get() || providers.stream().map(PrioritizedConfigurationProvider::getConfigurationProvider)
                .anyMatch(ConfigurationComponent::isDirty);
    }

    @Override
    public void invalidate() {
        providers.stream().map(PrioritizedConfigurationProvider::getConfigurationProvider)
                .forEach(ConfigurationComponent::invalidate);
    }

    @Override
    public Set<ConfigurationWatcher> watchers() {
        Set<ConfigurationWatcher> configurationWatchers = new HashSet<>();
        providers.stream().map(PrioritizedConfigurationProvider::getConfigurationProvider)
                .forEach(item -> configurationWatchers.addAll(item.watchers()));
        return configurationWatchers;
    }

    @Override
    public PrioritizedProvider fork() {
        PrioritizedProvider fork = new PrioritizedProvider();
        for (PrioritizedConfigurationProvider prioritizedConfigurationProvider : providers) {
            fork.registerProvider(
                    (ConfigurationProvider) prioritizedConfigurationProvider.getConfigurationProvider().fork(),
                    prioritizedConfigurationProvider.getPriority());
        }
        return fork;
    }

    public PrioritizedProvider registerProvider(ConfigurationProvider configurationProvider, int priority) {
        providers.add(new PrioritizedConfigurationProvider(priority, configurationProvider));
        dirty.set(true);
        return this;
    }

    private static class PrioritizedConfigurationProvider {
        private final int priority;
        private final ConfigurationProvider configurationProvider;

        private PrioritizedConfigurationProvider(int priority, ConfigurationProvider configurationProvider) {
            this.priority = priority;
            this.configurationProvider = configurationProvider;
        }

        int getPriority() {
            return priority;
        }

        ConfigurationProvider getConfigurationProvider() {
            return configurationProvider;
        }
    }
}
