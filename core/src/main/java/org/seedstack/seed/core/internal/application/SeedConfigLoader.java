/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.application;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import jodd.props.Props;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.MapConfiguration;
import org.javatuples.Pair;
import org.seedstack.seed.CoreErrorCode;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.core.utils.SeedReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Utility class which allows to load the application and Seed properties.
 *
 * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
 */
public class SeedConfigLoader {
    private static final String SEED_BOOTSTRAP_PROPS_PATH = "META-INF/configuration/seed.props";
    private static final String SEED_BOOTSTRAP_PROPERTIES_PATH = "META-INF/configuration/seed.properties";
    private static final Logger LOGGER = LoggerFactory.getLogger(SeedConfigLoader.class);

    /**
     * Build the configuration needed to bootstrap a Seed application.
     * <p>
     * This configuration is loaded from the seed-bootstrap.properties files.
     * If multiple files are present the configuration is concatenated.
     * </p>
     *
     * @return the bootstrap configuration
     */
    public Configuration buildBootstrapConfig() {
        Set<String> resources = Sets.newHashSet(SEED_BOOTSTRAP_PROPS_PATH, SEED_BOOTSTRAP_PROPERTIES_PATH);
        MapConfiguration globalConfiguration = buildConfig(resources, null).getValue0();
        globalConfiguration.getInterpolator().registerLookup("env", new EnvLookup());
        return new MapConfiguration(new ImmutableMap.Builder<String, Object>().putAll(globalConfiguration.getMap()).build());
    }

    /**
     * Build a unique configuration from a set of resources (props or properties) and an optional default configuration.
     *
     * @param configurationResources the paths to the configuration resources
     * @param defaultConfiguration   the default configuration registered with the SPI
     * @return the final configuration
     */
    public Pair<MapConfiguration, Props> buildConfig(Set<String> configurationResources, @Nullable Map<String, String> defaultConfiguration) {
        final Props props = buildProps();
        final Props propsOverride = buildProps();

        for (String configurationResource : configurationResources) {
            try {
                ClassLoader classLoader = SeedReflectionUtils.findMostCompleteClassLoader();
                if (classLoader == null) {
                    throw SeedException.createNew(CoreErrorCode.UNABLE_TO_FIND_CLASSLOADER);
                }
                Enumeration<URL> urlEnumeration = classLoader.getResources(configurationResource);
                while (urlEnumeration.hasMoreElements()) {
                    URL url = urlEnumeration.nextElement();
                    InputStream resourceAsStream = null;

                    try {
                        resourceAsStream = url.openStream();

                        if (isOverrideResource(configurationResource)) {
                            LOGGER.debug("Adding {} to configuration override", url.toExternalForm());
                            propsOverride.load(resourceAsStream);
                        } else {
                            LOGGER.debug("Adding {} to configuration", url.toExternalForm());
                            props.load(resourceAsStream);
                        }
                    } finally {
                        if (resourceAsStream != null) {
                            try {
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

        // Build configuration
        return Pair.with(buildConfiguration(props, propsOverride, defaultConfiguration), props);
    }

    /**
     * Gets the application active profiles.
     *
     * @return the array of active profiles or null if none is active
     */
    public String[] applicationProfiles() {
        return getStringArray(System.getProperty("org.seedstack.seed.profiles"));
    }

    /**
     * Indicates whether the resource path represents an override file, i.e. all the files ending with
     * ".override.properties" or the files ".override.props".
     *
     * @param configurationResource the path to test
     * @return true if the path correspond to an override file, false otherwise.
     */
    public boolean isOverrideResource(String configurationResource) {
        return configurationResource.endsWith(".override.properties") || configurationResource.endsWith(".override.props");
    }

    private Props buildProps() {
        Props newProps = new Props();

        newProps.setSkipEmptyProps(false);
        newProps.setAppendDuplicateProps(true);

        return newProps;
    }

    private MapConfiguration buildConfiguration(Props props, Props propsOverride, Map<String, String> defaultConfiguration) {
        Map<String, String> finalConfiguration = new HashMap<String, String>();
        Map<String, String> configurationMap = new HashMap<String, String>();
        Map<String, String> configurationOverrideMap = new HashMap<String, String>();

        // Extract props to maps
        props.extractProps(configurationMap, applicationProfiles());
        propsOverride.extractProps(configurationOverrideMap, applicationProfiles());

        // Put defaults to final configuration
        if (defaultConfiguration != null) {
            finalConfiguration.putAll(defaultConfiguration);
        }

        // Put nominal to final configuration
        finalConfiguration.putAll(configurationMap);

        applyPropertiesRemoval(finalConfiguration, configurationOverrideMap);

        // Put override to final configuration
        finalConfiguration.putAll(configurationOverrideMap);

        // Convert final configuration to an immutable Apache Commons Configuration
        return new MapConfiguration(new ImmutableMap.Builder<String, Object>().putAll(finalConfiguration).build());
    }

    /**
     * Looks the override config for properties starting with "-". These properties will be removed from the actual config.
     *
     * For instance the config contains:
     * <pre>
     * key1=foo
     * </pre>
     * and the override config contains:
     * <pre>
     * -key1
     * </pre>
     * The property will be removed from the final configuration
     *
     * @param config         the actual configuration
     * @param overrideConfig the configuration containing the overrides
     */
    private void applyPropertiesRemoval(Map<String, String> config, Map<String, String> overrideConfig) {
        Iterator<Map.Entry<String, String>> overrideIterator = overrideConfig.entrySet().iterator();
        while (overrideIterator.hasNext()) {
            String overrideKey = overrideIterator.next().getKey();
            if (overrideKey.startsWith("-")) {
                config.remove(overrideKey.substring(1));
                overrideIterator.remove();
            }
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
}
