/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.internal.configuration;

import com.google.common.collect.Sets;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.Context;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.api.plugin.request.ClasspathScanRequest;
import io.nuun.kernel.core.AbstractPlugin;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import org.seedstack.coffig.Coffig;
import org.seedstack.coffig.provider.InMemoryProvider;
import org.seedstack.coffig.provider.JacksonProvider;
import org.seedstack.coffig.provider.PropertiesProvider;
import org.seedstack.seed.Application;
import org.seedstack.seed.ApplicationConfig;
import org.seedstack.seed.ConfigConfig;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.core.SeedRuntime;
import org.seedstack.seed.core.internal.CoreErrorCode;
import org.seedstack.seed.diagnostic.DiagnosticManager;
import org.seedstack.seed.spi.ApplicationProvider;
import org.seedstack.seed.spi.ConfigurationPriority;
import org.seedstack.shed.ClassLoaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Core plugins that detects configuration files and adds them to the global configuration object.
 */
public class ConfigurationPlugin extends AbstractPlugin implements ApplicationProvider {
    public static final String NAME = "config";
    public static final String EXTERNAL_CONFIG_PREFIX = "seedstack.config.";
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationPlugin.class);
    private static final String CONFIGURATION_PACKAGE = "META-INF.configuration";
    private static final String CONFIGURATION_LOCATION = "META-INF/configuration/";
    private static final String YAML_REGEX = ".*\\.yaml";
    private static final String YML_REGEX = ".*\\.yml";
    private static final String JSON_REGEX = ".*\\.json";
    private static final String PROPERTIES_REGEX = ".*\\.properties";
    private SeedRuntime seedRuntime;
    private Coffig coffig;
    private DiagnosticManager diagnosticManager;
    private Application application;
    private ConfigConfig configConfig;

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void provideContainerContext(Object containerContext) {
        seedRuntime = (SeedRuntime) containerContext;
        coffig = seedRuntime.getConfiguration();
        diagnosticManager = seedRuntime.getDiagnosticManager();
    }

    @Override
    public String pluginPackageRoot() {
        ApplicationConfig applicationConfig = coffig.get(ApplicationConfig.class);
        if (applicationConfig.getBasePackages().isEmpty() && applicationConfig.isPackageScanWarning()) {
            LOGGER.warn("No base package configured, only classes in 'org.seedstack.*' packages will be scanned");
        }
        Set<String> basePackages = new HashSet<>(applicationConfig.getBasePackages());
        basePackages.add(CONFIGURATION_PACKAGE);
        return String.join(",", basePackages);
    }

    @Override
    public Collection<ClasspathScanRequest> classpathScanRequests() {
        return classpathScanRequestBuilder()
                .resourcesRegex(YAML_REGEX)
                .resourcesRegex(YML_REGEX)
                .resourcesRegex(JSON_REGEX)
                .resourcesRegex(PROPERTIES_REGEX)
                .build();
    }

    @SuppressWarnings("unchecked")
    @Override
    public InitState init(InitContext initContext) {
        detectSystemPropertiesConfig();
        detectKernelParamConfig(initContext);
        detectConfigurationFiles(initContext);

        application = new ApplicationImpl(coffig);
        configConfig = coffig.get(ConfigConfig.class);

        diagnosticManager.registerDiagnosticInfoCollector("application",
                new ApplicationDiagnosticCollector(application));

        return InitState.INITIALIZED;
    }

    @Override
    public void start(Context context) {
        int watchPeriod = configConfig.getWatchPeriod();
        if (watchPeriod > 0) {
            LOGGER.info("Watching configuration changes every {} second(s)", watchPeriod);
            coffig.startWatching(watchPeriod);
        }
    }

    @Override
    public void stop() {
        coffig.stopWatching();
    }

    private void detectKernelParamConfig(InitContext initContext) {
        InMemoryProvider kernelParamConfigProvider = new InMemoryProvider();

        for (Map.Entry<String, String> kernelParam : initContext.kernelParams().entrySet()) {
            if (kernelParam.getKey().startsWith(EXTERNAL_CONFIG_PREFIX)) {
                addValue(kernelParamConfigProvider, kernelParam.getKey(), kernelParam.getValue());
            }
        }

        seedRuntime.registerConfigurationProvider(
                kernelParamConfigProvider,
                ConfigurationPriority.KERNEL_PARAMETERS_CONFIG
        );
    }

    private void addValue(InMemoryProvider inMemoryProvider, String key, String value) {
        String choppedKey = key.substring(EXTERNAL_CONFIG_PREFIX.length());
        if (value.contains(",")) {
            String[] values = Arrays.stream(value.split(",")).map(String::trim).toArray(String[]::new);
            LOGGER.debug("External array configuration property: {}={}", choppedKey, Arrays.toString(values));
            inMemoryProvider.put(choppedKey, values);
        } else {
            LOGGER.debug("External configuration property: {}={}", choppedKey, value);
            inMemoryProvider.put(choppedKey, value);
        }
    }

    private void detectSystemPropertiesConfig() {
        InMemoryProvider systemPropertiesProvider = new InMemoryProvider();

        Properties systemProperties = System.getProperties();
        for (String systemProperty : systemProperties.stringPropertyNames()) {
            if (systemProperty.startsWith(EXTERNAL_CONFIG_PREFIX)) {
                addValue(systemPropertiesProvider, systemProperty, systemProperties.getProperty(systemProperty));
            }
        }

        seedRuntime.registerConfigurationProvider(
                systemPropertiesProvider,
                ConfigurationPriority.SYSTEM_PROPERTIES_CONFIG
        );
    }

    private void detectConfigurationFiles(InitContext initContext) {
        JacksonProvider jacksonProvider = new JacksonProvider();
        JacksonProvider jacksonOverrideProvider = new JacksonProvider();
        PropertiesProvider propertiesProvider = new PropertiesProvider();
        PropertiesProvider propertiesOverrideProvider = new PropertiesProvider();

        for (String configurationResource : retrieveConfigurationResources(initContext)) {
            try {
                ClassLoader classLoader = ClassLoaders.findMostCompleteClassLoader();
                Enumeration<URL> urlEnumeration = classLoader.getResources(configurationResource);
                while (urlEnumeration.hasMoreElements()) {
                    URL url = urlEnumeration.nextElement();

                    if (isOverrideResource(configurationResource)) {
                        LOGGER.debug("Detected override configuration resource: {}", url.toExternalForm());

                        if (isJacksonResource(configurationResource)) {
                            jacksonOverrideProvider.addSource(url);
                        } else if (isPropertiesResource(configurationResource)) {
                            propertiesOverrideProvider.addSource(url);
                        } else {
                            LOGGER.warn("Unrecognized override configuration resource: {}", url.toExternalForm());
                        }
                    } else {
                        LOGGER.debug("Detected configuration resource: {}", url.toExternalForm());

                        if (isJacksonResource(configurationResource)) {
                            jacksonProvider.addSource(url);
                        } else if (isPropertiesResource(configurationResource)) {
                            propertiesProvider.addSource(url);
                        } else {
                            LOGGER.warn("Unrecognized configuration resource: {}", url.toExternalForm());
                        }
                    }
                }
            } catch (IOException e) {
                throw SeedException.wrap(e, CoreErrorCode.UNABLE_TO_LOAD_CONFIGURATION_RESOURCE).put("resource",
                        configurationResource);
            }
        }

        seedRuntime.registerConfigurationProvider(
                jacksonProvider,
                ConfigurationPriority.SCANNED
        );
        seedRuntime.registerConfigurationProvider(
                propertiesProvider,
                ConfigurationPriority.SCANNED
        );
        seedRuntime.registerConfigurationProvider(
                jacksonOverrideProvider,
                ConfigurationPriority.SCANNED_OVERRIDE
        );
        seedRuntime.registerConfigurationProvider(
                propertiesOverrideProvider,
                ConfigurationPriority.SCANNED_OVERRIDE
        );
    }

    private Set<String> retrieveConfigurationResources(InitContext initContext) {
        Set<String> allConfigurationResources = Sets.newHashSet();
        allConfigurationResources.addAll(collectConfigResources(initContext, YAML_REGEX));
        allConfigurationResources.addAll(collectConfigResources(initContext, YML_REGEX));
        allConfigurationResources.addAll(collectConfigResources(initContext, JSON_REGEX));
        allConfigurationResources.addAll(collectConfigResources(initContext, PROPERTIES_REGEX));
        return allConfigurationResources;
    }

    private List<String> collectConfigResources(InitContext initContext, String regex) {
        return initContext.mapResourcesByRegex()
                .get(regex)
                .stream()
                .filter(propsResource -> propsResource.startsWith(CONFIGURATION_LOCATION))
                .collect(Collectors.toList());
    }

    private boolean isOverrideResource(String configurationResource) {
        return configurationResource.endsWith(".override.yaml") ||
                configurationResource.endsWith(".override.yml") ||
                configurationResource.endsWith(".override.json") ||
                configurationResource.endsWith(".override.properties");
    }

    private boolean isJacksonResource(String configurationResource) {
        return configurationResource.endsWith(".yaml") ||
                configurationResource.endsWith(".yml") ||
                configurationResource.endsWith(".json");
    }

    private boolean isPropertiesResource(String configurationResource) {
        return configurationResource.endsWith(".properties");
    }

    @Override
    public Object nativeUnitModule() {
        return new ConfigurationModule(application);
    }

    @Override
    public Application getApplication() {
        return application;
    }
}
