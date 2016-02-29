/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed;

import io.nuun.kernel.api.Plugin;
import org.seedstack.coffig.Coffig;
import org.seedstack.coffig.provider.CompositeProvider;
import org.seedstack.coffig.provider.InMemoryProvider;
import org.seedstack.coffig.provider.PrioritizedProvider;
import org.seedstack.seed.spi.diagnostic.DiagnosticInfoCollector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

public class SeedRuntime {
    private static final String SEED_PACKAGE_PREFIX = "org.seedstack.seed";
    private static final int DEFAULT_CONFIGURATION_PRIORITY = -1000;

    private final Object context;
    private final DiagnosticManager diagnosticManager;
    private final Coffig configuration;
    private final InMemoryProvider defaultConfigurationProvider;
    private final boolean colorOutputSupported;
    private final String seedVersion;
    private final Set<String> inconsistentPlugins = new HashSet<>();

    private SeedRuntime(Object context, DiagnosticManager diagnosticManager, Coffig configuration, boolean colorOutputSupported) {
        this.context = context;
        this.diagnosticManager = diagnosticManager;
        this.configuration = configuration;
        this.colorOutputSupported = colorOutputSupported;
        this.seedVersion = SeedRuntime.class.getPackage() == null ? null : SeedRuntime.class.getPackage().getImplementationVersion();

        checkConsistency();

        this.diagnosticManager.registerDiagnosticInfoCollector("seed", new RuntimeDiagnosticInfoCollector());
        ((CompositeProvider) this.configuration.getProvider()).get(PrioritizedProvider.class).registerProvider("default", defaultConfigurationProvider = new InMemoryProvider(), DEFAULT_CONFIGURATION_PRIORITY);
    }

    public <T> T contextAs(Class<T> tClass) {
        if (context != null && tClass.isAssignableFrom(context.getClass())) {
            return tClass.cast(context);
        } else {
            return null;
        }
    }

    public DiagnosticManager getDiagnosticManager() {
        return diagnosticManager;
    }

    public Coffig getConfiguration() {
        return configuration;
    }

    public boolean isColorOutputSupported() {
        return colorOutputSupported;
    }

    public String getVersion() {
        return seedVersion;
    }

    public InMemoryProvider getDefaultConfigurationProvider() {
        return defaultConfigurationProvider;
    }

    private void checkConsistency() {
        if (seedVersion != null) {
            for (Plugin plugin : ServiceLoader.load(Plugin.class)) {
                Class<? extends Plugin> pluginClass = plugin.getClass();
                Package pluginPackage = pluginClass.getPackage();

                if (pluginPackage != null && pluginPackage.getName().startsWith(SEED_PACKAGE_PREFIX)) {
                    String pluginVersion = pluginPackage.getImplementationVersion();
                    if (pluginVersion != null && !pluginVersion.equals(seedVersion)) {
                        inconsistentPlugins.add(plugin.name());
                    }
                }
            }
        }
    }

    public static class Builder {
        private Object _context;
        private DiagnosticManager _diagnosticManager;
        private Coffig _configuration;
        private boolean _colorSupported;

        private Builder() {
        }

        public Builder context(Object context) {
            this._context = context;
            return this;
        }

        public Builder diagnosticManager(DiagnosticManager diagnosticManager) {
            this._diagnosticManager = diagnosticManager;
            return this;
        }

        public Builder colorSupported(boolean colorSupported) {
            this._colorSupported = colorSupported;
            return this;
        }

        public Builder configuration(Coffig configuration) {
            this._configuration = configuration;
            return this;
        }

        public SeedRuntime build() {
            return new SeedRuntime(_context, _diagnosticManager, _configuration, _colorSupported);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    private class RuntimeDiagnosticInfoCollector implements DiagnosticInfoCollector {
        @Override
        public Map<String, Object> collect() {
            Map<String, Object> result = new HashMap<>();

            if (seedVersion != null) {
                result.put("version", seedVersion);
            }

            if (!inconsistentPlugins.isEmpty()) {
                result.put("inconsistent-plugins", inconsistentPlugins);
            }

            if (context != null) {
                result.put("context", context);
            }

            if (configuration != null) {
                result.put("configuration", configuration.toString());
            }

            result.put("color-output-supported", colorOutputSupported);

            return result;
        }
    }
}
