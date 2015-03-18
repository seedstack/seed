/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.application;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import org.apache.commons.lang.text.StrLookup;
import org.seedstack.seed.core.api.Application;
import org.seedstack.seed.core.api.SeedException;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.api.plugin.request.ClasspathScanRequest;
import io.nuun.kernel.core.AbstractPlugin;
import jodd.props.Props;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.MapConfiguration;
import org.seedstack.seed.core.internal.CorePlugin;
import org.seedstack.seed.core.utils.SeedReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Plugin that initialize the application identity, storage location and configuration.
 *
 * @author adrien.lauer@mpsa.com
 */
public class ApplicationPlugin extends AbstractPlugin {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationPlugin.class);
    private static final String CONFIGURATION_LOCATION = "META-INF/configuration/";
    static final String PROPS_REGEX = ".*\\.props";
    static final String PROPERTIES_REGEX = ".*\\.properties";

    private final Map<String, String> defaultConfiguration = new ConcurrentHashMap<String, String>();
    private final ClassLoader classLoader = SeedReflectionUtils.findMostCompleteClassLoader(ApplicationPlugin.class);
    private final Props props = buildProps();
    private final Props propsOverride = buildProps();

    private Application application;

    @Override
    public String name() {
        return "seed-core-application-plugin";
    }

    @Override
    public String pluginPackageRoot() {
        return "META-INF.configuration";
    }

    @Override
    public InitState init(InitContext initContext) {
        Set<String> allConfigurationResources = Sets.newHashSet();

        for (String propertiesResource : initContext.mapResourcesByRegex().get(PROPERTIES_REGEX)) {
            if (propertiesResource.startsWith(CONFIGURATION_LOCATION)) {
                allConfigurationResources.add(propertiesResource);
            }
        }

        for (String propsResource : initContext.mapResourcesByRegex().get(PROPS_REGEX)) {
            if (propsResource.startsWith(CONFIGURATION_LOCATION)) {
                allConfigurationResources.add(propsResource);
            }
        }

        for (String configurationResource : allConfigurationResources) {
            boolean isOverrideResource = configurationResource.endsWith(".override.properties") || configurationResource.endsWith(".override.props");

            try {
                Enumeration<URL> urlEnumeration = classLoader.getResources(configurationResource);
                while (urlEnumeration.hasMoreElements()) {
                    URL url = urlEnumeration.nextElement();
                    InputStream resourceAsStream = null;

                    try {
                        resourceAsStream = url.openStream();

                        if (isOverrideResource) {
                            LOGGER.debug("Adding {} to configuration override", url.toExternalForm());
                            propsOverride.load(resourceAsStream);
                        } else {
                            LOGGER.debug("Adding {} to configuration", url.toExternalForm());
                            props.load(resourceAsStream);
                        }
                    } finally {
                        if (resourceAsStream != null) {
                            try { // NOSONAR
                                resourceAsStream.close();
                            } catch (IOException e) {
                                LOGGER.warn("Unable to close configuration resource " + configurationResource, e);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                throw SeedException.wrap(e, ApplicationErrorCode.UNABLE_TO_LOAD_CONFIGURATION_RESOURCE).put("resource", configurationResource);
            }
        }

        // Determine configuration profile
        String[] profiles = getStringArray(System.getProperty("org.seedstack.seed.profiles"));
        if (profiles == null || profiles.length == 0) {
            LOGGER.info("No configuration profile selected");
        } else {
            LOGGER.info("Active configuration profile(s): {}", Arrays.toString(profiles));
        }

        // Build configuration
        Configuration configuration = buildConfiguration(props, propsOverride, profiles);
        Configuration coreConfiguration = configuration.subset(CorePlugin.CORE_PLUGIN_PREFIX);

        String appId = coreConfiguration.getString("application-id");
        if (appId == null || appId.isEmpty()) {
            throw SeedException.createNew(ApplicationErrorCode.MISSING_APPLICATION_IDENTIFIER).put("property", CorePlugin.CORE_PLUGIN_PREFIX + ".application-id");
        }

        String appName = coreConfiguration.getString("application-name");
        if (appName == null) {
            appName = appId;
        }

        String appVersion = coreConfiguration.getString("application-version");
        if (appVersion == null) {
            appVersion = "0.0.0";
        }

        LOGGER.info("Application info: '{}' / '{}' / '{}'", appId, appName, appVersion);

        String seedStorage = coreConfiguration.getString("storage");
        File seedDirectory;
        if (seedStorage == null) {
            seedDirectory = new File(new File(getUserHome(), ".seed"), appId);
        } else {
            seedDirectory = new File(seedStorage);
        }

        if (!seedDirectory.exists() && !seedDirectory.mkdirs()) {
            throw SeedException.createNew(ApplicationErrorCode.UNABLE_TO_CREATE_STORAGE_DIRECTORY).put("path", seedDirectory.getAbsolutePath());
        }

        if (!seedDirectory.isDirectory()) {
            throw SeedException.createNew(ApplicationErrorCode.STORAGE_PATH_IS_NOT_A_DIRECTORY).put("path", seedDirectory.getAbsolutePath());
        }

        if (!seedDirectory.canWrite()) {
            throw SeedException.createNew(ApplicationErrorCode.STORAGE_DIRECTORY_IS_NOT_WRITABLE).put("path", seedDirectory.getAbsolutePath());
        }

        LOGGER.debug("Application storage location is {}", seedDirectory.getAbsolutePath());

        if (coreConfiguration.getBoolean("redirect-jul", true)) {
            SLF4JBridgeHandler.removeHandlersForRootLogger();
            SLF4JBridgeHandler.install();

            LOGGER.debug("Java logging to SLF4J redirection enabled, if you're using logback be sure to have a LevelChangePropagator in your configuration");
        }

        this.application = new ApplicationImpl(appName, appId, appVersion, seedDirectory, configuration);

        return InitState.INITIALIZED;
    }

    @Override
    public Collection<ClasspathScanRequest> classpathScanRequests() {
        return classpathScanRequestBuilder()
                .resourcesRegex(PROPERTIES_REGEX)
                .resourcesRegex(PROPS_REGEX)
                .build();
    }

    @Override
    public Object nativeUnitModule() {
        return new ApplicationModule(this.application);
    }

    /**
     * Retrieve the application object.
     *
     * @return the application object.
     */
    public Application getApplication() {
        return this.application;
    }

    /**
     * Return the inner props configuration.
     *
     * @return the inner props configuration.
     */
    public Props getProps() {
        return this.props;
    }

    /**
     * Return the default configuration.
     *
     * @return the default configuration.
     */
    public Map<String, String> getDefaultConfiguration() {
        return defaultConfiguration;
    }

    private String getUserHome() {
        String userProfile = System.getenv("USERPROFILE");
        if (userProfile != null) {
            return userProfile;
        } else {
            return System.getProperty("user.home");
        }
    }

    private String[] getStringArray(String value) {
        if (value == null) {
            return null;
        } else {
            String[] split = value.split(",");
            for (int i = 0; i < split.length; i++) {
                split[i] = split[i].trim();
            }

            if (split.length == 0) {
                return null;
            } else {
                return split;
            }
        }
    }

    private Props buildProps() {
        Props newProps = new Props();

        newProps.setSkipEmptyProps(false);
        newProps.setAppendDuplicateProps(true);

        return newProps;
    }

    private Configuration buildConfiguration(Props props, Props propsOverride, String... profiles) {
        Map<String, String> finalConfiguration = new HashMap<String, String>();
        Map<String, String> configurationMap = new HashMap<String, String>();
        Map<String, String> configurationOverrideMap = new HashMap<String, String>();

        // Extract props to maps
        props.extractProps(configurationMap, profiles);
        propsOverride.extractProps(configurationOverrideMap, profiles);

        // Put defaults to final configuration
        finalConfiguration.putAll(defaultConfiguration);

        // Put nominal to final configuration
        finalConfiguration.putAll(configurationMap);

        // Apply removal behavior
        Iterator<Map.Entry<String, String>> overrideIterator = configurationOverrideMap.entrySet().iterator();
        while (overrideIterator.hasNext()) {
            String overrideKey = overrideIterator.next().getKey();
            if (overrideKey.startsWith("-")) {
                finalConfiguration.remove(overrideKey.substring(1));
                overrideIterator.remove();
            }
        }

        // Put override to final configuration
        finalConfiguration.putAll(configurationOverrideMap);

        // Convert final configuration to an immutable Apache Commons Configuration
        MapConfiguration mapConfiguration = new MapConfiguration(new ImmutableMap.Builder<String, Object>().putAll(finalConfiguration).build());
        mapConfiguration.getInterpolator().registerLookup("env", StrLookup.mapLookup(System.getenv()));
        mapConfiguration.getInterpolator().registerLookup("json", new JsonLookup(mapConfiguration));
        return mapConfiguration;
    }
}
