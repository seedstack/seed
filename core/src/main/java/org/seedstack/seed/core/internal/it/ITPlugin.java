/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.it;

import static org.seedstack.shed.ClassLoaders.findMostCompleteClassLoader;

import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.PluginException;
import io.nuun.kernel.api.plugin.context.InitContext;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.ServiceLoader;
import java.util.Set;
import org.seedstack.seed.core.SeedRuntime;
import org.seedstack.seed.core.internal.AbstractSeedPlugin;
import org.seedstack.seed.testing.spi.TestDecorator;
import org.seedstack.seed.testing.spi.TestPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This plugin automatically enable integration tests to be managed by SeedStack.
 */
public class ITPlugin extends AbstractSeedPlugin {
    private static final String TEST_CLASS_KERNEL_PARAMETER = "seedstack.it.testClassName";
    private static final String APPLICATION_STORAGE = "application.storage";
    private static final String CONFIG_WATCH = "config.watch";
    private static final Logger LOGGER = LoggerFactory.getLogger(ITPlugin.class);
    private final Set<Class<? extends TestDecorator>> testDecorators = new HashSet<>();
    private File temporaryAppStorage;
    private Class<?> testClass;

    @Override
    public String name() {
        return "testing";
    }

    @Override
    public void setup(SeedRuntime seedRuntime) {
        // Create temporary directory for application storage
        try {
            temporaryAppStorage = Files.createTempDirectory("seedstack-it-").toFile();
            LOGGER.debug("Created temporary application storage directory {}", temporaryAppStorage.getAbsolutePath());
            seedRuntime.setDefaultConfiguration(APPLICATION_STORAGE, temporaryAppStorage.getAbsolutePath());
            seedRuntime.setDefaultConfiguration(CONFIG_WATCH, "false");
        } catch (IOException e) {
            LOGGER.warn("Unable to create temporary application storage directory");
        }
    }

    @Override
    public InitState initialize(InitContext initContext) {
        String itClassName = initContext.kernelParam(TEST_CLASS_KERNEL_PARAMETER);

        if (itClassName != null) {
            try {
                testClass = Class.forName(itClassName);
            } catch (ClassNotFoundException e) {
                throw new PluginException("Unable to load the test class " + itClassName, e);
            }
        }

        for (TestPlugin plugin : ServiceLoader.load(TestPlugin.class, findMostCompleteClassLoader(ITPlugin.class))) {
            testDecorators.addAll(plugin.decorators());
        }

        return InitState.INITIALIZED;
    }

    @Override
    public Object nativeUnitModule() {
        return new ITModule(testClass, testDecorators);
    }

    @Override
    public void stop() {
        try {
            if (temporaryAppStorage != null) {
                deleteRecursively(temporaryAppStorage);
                LOGGER.debug("Deleted temporary application storage directory {}",
                        temporaryAppStorage.getAbsolutePath());
            }
        } catch (Exception e) {
            LOGGER.warn(
                    "Unable to delete temporary application storage directory " + temporaryAppStorage.getAbsolutePath(),
                    e);
        }
    }

    private void deleteRecursively(final File file) throws IOException {
        if (!file.exists()) {
            return;
        }

        // We do not want to traverse symbolic links to directories, we simply unlink them
        if (file.isDirectory() && !Files.isSymbolicLink(file.toPath())) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File c : files) {
                    deleteRecursively(c);
                }
            }
        }

        if (!file.delete()) {
            LOGGER.warn("Unable to delete file {}" + file.getAbsolutePath());
        }
    }
}
