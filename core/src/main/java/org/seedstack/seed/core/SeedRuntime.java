/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import io.nuun.kernel.api.Plugin;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import javax.validation.ValidatorFactory;
import org.seedstack.coffig.Coffig;
import org.seedstack.coffig.provider.CompositeProvider;
import org.seedstack.coffig.provider.InMemoryProvider;
import org.seedstack.coffig.spi.ConfigurationProvider;
import org.seedstack.seed.core.internal.configuration.PrioritizedProvider;
import org.seedstack.seed.diagnostic.DiagnosticManager;
import org.seedstack.seed.diagnostic.spi.DiagnosticInfoCollector;
import org.seedstack.seed.spi.ConfigurationPriority;

public class SeedRuntime {
    private static final String SEED_PACKAGE_PREFIX = "org.seedstack.seed";
    private static final YAMLMapper yamlMapper = new YAMLMapper();
    private final Object context;
    private final DiagnosticManager diagnosticManager;
    private final Coffig configuration;
    private final InMemoryProvider inMemoryProvider;
    private final PrioritizedProvider prioritizedProvider;
    private final ValidatorFactory validatorFactory;
    private final String seedVersion;
    private final String businessVersion;
    private final Set<String> inconsistentPlugins = new HashSet<>();

    private SeedRuntime(Object context, DiagnosticManager diagnosticManager, Coffig configuration,
            ValidatorFactory validatorFactory, String seedVersion, String businessVersion) {
        this.context = context;
        this.diagnosticManager = diagnosticManager;
        this.configuration = configuration;
        this.validatorFactory = validatorFactory;
        this.seedVersion = seedVersion;
        this.businessVersion = businessVersion;
        this.diagnosticManager.registerDiagnosticInfoCollector("seed", new RuntimeDiagnosticCollector());
        this.prioritizedProvider = ((CompositeProvider) this.configuration.getProvider()).get(
                PrioritizedProvider.class);
        this.inMemoryProvider = new InMemoryProvider();
        registerConfigurationProvider(this.inMemoryProvider, ConfigurationPriority.DEFAULT);
        checkConsistency();
    }

    public static Builder builder() {
        return new Builder();
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

    public void registerConfigurationProvider(ConfigurationProvider configurationProvider, int priority) {
        prioritizedProvider.registerProvider(configurationProvider, priority);
    }

    public void setDefaultConfiguration(String key, String value) {
        inMemoryProvider.put(key, value);
    }

    public void setDefaultConfiguration(String key, String... values) {
        inMemoryProvider.put(key, values);
    }

    public void setDefaultConfiguration(String key, Collection<String> values) {
        inMemoryProvider.put(key, values);
    }

    public ValidatorFactory getValidatorFactory() {
        return validatorFactory;
    }

    public String getVersion() {
        return seedVersion;
    }

    public String getBusinessVersion() {
        return businessVersion;
    }

    private void checkConsistency() {
        if (seedVersion != null) {
            for (Plugin plugin : ServiceLoader.load(Plugin.class)) {
                Class<? extends Plugin> pluginClass = plugin.getClass();
                Package pluginPackage = pluginClass.getPackage();

                if (pluginPackage != null && pluginPackage.getName().startsWith(SEED_PACKAGE_PREFIX)) {
                    String pluginVersion = pluginPackage.getImplementationVersion();
                    if (!seedVersion.equals(pluginVersion)) {
                        inconsistentPlugins.add(plugin.name());
                    }
                }
            }
        }
    }

    public static class Builder {
        private Object _context;
        private Coffig _configuration;
        private DiagnosticManager _diagnosticManager;
        private ValidatorFactory _validatorFactory;
        private String _seedVersion;
        private String _businessVersion;

        private Builder() {
        }

        public Builder context(Object context) {
            this._context = context;
            return this;
        }

        public Builder configuration(Coffig configuration) {
            this._configuration = configuration;
            return this;
        }

        public Builder diagnosticManager(DiagnosticManager diagnosticManager) {
            this._diagnosticManager = diagnosticManager;
            return this;
        }

        public Builder validatorFactory(ValidatorFactory validatorFactory) {
            this._validatorFactory = validatorFactory;
            return this;
        }

        public Builder version(String seedVersion) {
            this._seedVersion = seedVersion;
            return this;
        }

        public Builder businessVersion(String businessVersion) {
            this._businessVersion = businessVersion;
            return this;
        }

        public SeedRuntime build() {
            return new SeedRuntime(
                    _context,
                    _diagnosticManager,
                    _configuration,
                    _validatorFactory,
                    _seedVersion,
                    _businessVersion
            );
        }
    }

    private class RuntimeDiagnosticCollector implements DiagnosticInfoCollector {
        @Override
        public Map<String, Object> collect() {
            Map<String, Object> result = new HashMap<>();

            result.put("version", seedVersion == null ? "UNKNOWN" : seedVersion);
            result.put("businessVersion", businessVersion == null ? "UNKNOWN" : businessVersion);
            result.put("inconsistentPlugins", inconsistentPlugins);
            result.put("contextClass", context == null ? "NONE" : context.getClass().getName());
            try {
                result.put("configuration", yamlMapper.readValue(configuration.toString(), Map.class));
            } catch (IOException | RuntimeException e) {
                result.put("rawConfiguration", configuration.toString());
            }

            return result;
        }
    }
}
