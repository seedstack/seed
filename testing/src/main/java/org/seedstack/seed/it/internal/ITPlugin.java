/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.it.internal;

import com.google.common.io.Files;
import io.nuun.kernel.api.Plugin;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.PluginException;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.api.plugin.request.ClasspathScanRequest;
import io.nuun.kernel.core.AbstractPlugin;
import org.kametic.specifications.Specification;
import org.seedstack.seed.core.internal.application.ApplicationPlugin;
import org.seedstack.seed.it.api.ITBind;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

/**
 * This plugin automatically enable integration tests to be managed by SEED.
 *
 * @author redouane.loulou@ext.mpsa.com
 * @author adrien.lauer@mpsa.com
 * @author epo.jemba@ext.mpsa.com
 */
public class ITPlugin extends AbstractPlugin {
    public static final String IT_CLASS_NAME = "org.seedstack.seed.it.class.name";
    public static final String DEFAULT_CONFIGURATION_PREFIX = "org.seedstack.seed.it.default-configuration.";

    private static final Logger LOGGER = LoggerFactory.getLogger(ITPlugin.class);

    private final Collection<Class<?>> integrationTestsClass = new ArrayList<Class<?>>();
    private File appStorage;
    private Class<?> testClass;

    @SuppressWarnings("unchecked")
    private final Specification<Class<?>> iTSpecs = or(classAnnotatedWith(ITBind.class));

    @Override
    public String name() {
        return "seed-it-plugin";
    }

    @Override
    @SuppressWarnings("rawtypes")
    public InitState init(InitContext initContext) {
        String itClassName = initContext.kernelParam(IT_CLASS_NAME);
        Map<String, String> defaultConfiguration = ((ApplicationPlugin) initContext.dependentPlugins().iterator().next()).getDefaultConfiguration();

        // Automatically define a unique identifier for this test (can be overridden by explicit identifier in the configuration)
        defaultConfiguration.put("org.seedstack.seed.core.application-id", UUID.randomUUID().toString());

        // Create temporary directory for application storage
        appStorage = Files.createTempDir();
        LOGGER.info("Created temporary application storage directory {}", appStorage.getAbsolutePath());
        defaultConfiguration.put("org.seedstack.seed.core.storage", appStorage.getAbsolutePath());

        // Process default configuration
        for (String key : System.getProperties().stringPropertyNames()) {
            if (key.startsWith(DEFAULT_CONFIGURATION_PREFIX)) {
                defaultConfiguration.put(key.substring(DEFAULT_CONFIGURATION_PREFIX.length()), System.getProperty(key));
            }
        }

        // For ITBind classes binding
        Map<Specification, Collection<Class<?>>> scannedTypesBySpecification = initContext.scannedTypesBySpecification();
        Collection<Class<?>> iTClassCandidates = scannedTypesBySpecification.get(iTSpecs);
        if (iTClassCandidates != null && !iTClassCandidates.isEmpty()) {
            for (Class<?> itCandidate : iTClassCandidates) {
                if (itCandidate.getAnnotation(ITBind.class) != null) {
                    integrationTestsClass.add(itCandidate);
                }
            }
        }

        // For test class binding
        if (itClassName != null) {
            try {
                testClass = Class.forName(itClassName);
            } catch (ClassNotFoundException e) {
                throw new PluginException("Unable to load the test class " + itClassName, e);
            }
        }

        return InitState.INITIALIZED;
    }

    @Override
    public void stop() {
        try {
            deleteRecursively(appStorage);
            LOGGER.info("Deleted temporary application storage directory {}", appStorage.getAbsolutePath());
        } catch (Exception e) {
            LOGGER.warn("Unable to delete temporary application storage directory " + appStorage.getAbsolutePath(), e);
        }
    }

    @Override
    public Collection<ClasspathScanRequest> classpathScanRequests() {
        return classpathScanRequestBuilder().specification(iTSpecs).build();
    }

    @Override
    public Object nativeUnitModule() {
        return new ITModule(testClass, integrationTestsClass);
    }

    @Override
    public Collection<Class<? extends Plugin>> dependentPlugins() {
        Collection<Class<? extends Plugin>> plugins = new ArrayList<Class<? extends Plugin>>();
        plugins.add(ApplicationPlugin.class);
        return plugins;
    }

    private void deleteRecursively(final File file) throws IOException {
        if (!file.exists()) {
            return;
        }

        // TODO when building for Java 7 or later, use symlink detection, meanwhile let's hope the temporary IT directory doesn't contain any

        if (file.isDirectory()) {
            File[] files = file.listFiles();

            if (files != null) {
                for (File c : files) {
                    deleteRecursively(c);
                }
            }
        }

        if (!file.delete()) {
            LOGGER.debug("Unable to delete file {}" + file.getAbsolutePath());
        }
    }
}
