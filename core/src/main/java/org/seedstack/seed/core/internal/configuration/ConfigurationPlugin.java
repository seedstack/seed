/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.configuration;

import com.google.common.collect.Sets;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.api.plugin.request.ClasspathScanRequest;
import io.nuun.kernel.core.AbstractPlugin;
import org.seedstack.coffig.Coffig;
import org.seedstack.coffig.provider.CompositeProvider;
import org.seedstack.coffig.provider.JacksonProvider;
import org.seedstack.coffig.spi.ConfigurationProvider;
import org.seedstack.seed.Application;
import org.seedstack.seed.ApplicationConfig;
import org.seedstack.seed.DiagnosticManager;
import org.seedstack.seed.core.Seed;
import org.seedstack.seed.core.SeedRuntime;
import org.seedstack.seed.core.internal.CoreErrorCode;
import org.seedstack.seed.core.utils.SeedReflectionUtils;
import org.seedstack.seed.spi.config.ApplicationProvider;
import org.seedstack.seed.SeedException;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Core plugins that detects configuration files and adds them to the global configuration object.
 */
public class ConfigurationPlugin extends AbstractPlugin implements ApplicationProvider {
    private static final String CONFIGURATION_PACKAGE = "META-INF.configuration";
    private static final String CONFIGURATION_LOCATION = "META-INF/configuration/";
    private static final String YAML_REGEX = ".*\\.yaml";
    private static final String JSON_REGEX = ".*\\.json";

    private Coffig configuration;
    private DiagnosticManager diagnosticManager;
    private ApplicationConfig applicationConfig;
    private Application application;

    @Override
    public String name() {
        return "config";
    }

    @Override
    public void provideContainerContext(Object containerContext) {
        SeedRuntime seedRuntime = (SeedRuntime) containerContext;
        configuration = seedRuntime.getConfiguration();
        diagnosticManager = seedRuntime.getDiagnosticManager();
    }

    @Override
    public String pluginPackageRoot() {
        applicationConfig = configuration.get(ApplicationConfig.class);
        Set<String> basePackages = new HashSet<>(applicationConfig.getBasePackages());
        basePackages.add(CONFIGURATION_PACKAGE);
        return String.join(",", basePackages);
    }

    @Override
    public Collection<ClasspathScanRequest> classpathScanRequests() {
        return classpathScanRequestBuilder()
                .resourcesRegex(YAML_REGEX)
                .resourcesRegex(JSON_REGEX)
                .build();
    }

    @SuppressWarnings("unchecked")
    @Override
    public InitState init(InitContext initContext) {
        detectConfigurationFiles(initContext);

        application = new ApplicationImpl(applicationConfig, configuration);
        diagnosticManager.registerDiagnosticInfoCollector("core", new ConfigurationDiagnosticCollector(application));

        return InitState.INITIALIZED;
    }

    private void detectConfigurationFiles(InitContext initContext) {
        JacksonProvider jacksonProvider = new JacksonProvider();
        JacksonProvider jacksonOverrideProvider = new JacksonProvider();

        for (String configurationResource : retrieveConfigurationResources(initContext)) {
            try {
                ClassLoader classLoader = SeedReflectionUtils.findMostCompleteClassLoader();
                Enumeration<URL> urlEnumeration = classLoader.getResources(configurationResource);
                while (urlEnumeration.hasMoreElements()) {
                    if (isOverrideResource(configurationResource)) {
                        jacksonOverrideProvider.addSource(urlEnumeration.nextElement());
                    } else {
                        jacksonProvider.addSource(urlEnumeration.nextElement());
                    }
                }
            } catch (IOException e) {
                throw SeedException.wrap(e, CoreErrorCode.UNABLE_TO_LOAD_CONFIGURATION_RESOURCE).put("resource", configurationResource);
            }
        }

        ConfigurationProvider configurationProvider = configuration.getProvider();
        ((CompositeProvider) configurationProvider).get(PrioritizedProvider.class)
                .registerProvider("scanned", jacksonProvider, Seed.CONFIGURATION_BASE_PRIORITY - 1)
                .registerProvider("scanned-override", jacksonProvider, Seed.CONFIGURATION_OVERRIDE_PRIORITY - 1);
    }

    private Set<String> retrieveConfigurationResources(InitContext initContext) {
        Set<String> allConfigurationResources = Sets.newHashSet();
        allConfigurationResources.addAll(collectConfigResources(initContext, YAML_REGEX));
        allConfigurationResources.addAll(collectConfigResources(initContext, JSON_REGEX));
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
        return configurationResource.endsWith(".override.yaml") || configurationResource.endsWith(".override.json");
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
