/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.it.internal;

import com.google.common.io.Files;
import com.google.inject.Module;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.PluginException;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.api.plugin.request.ClasspathScanRequest;
import io.nuun.kernel.core.AbstractPlugin;
import org.kametic.specifications.Specification;
import org.seedstack.coffig.provider.InMemoryProvider;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.core.SeedRuntime;
import org.seedstack.seed.it.ITBind;
import org.seedstack.seed.it.ITInstall;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This plugin automatically enable integration tests to be managed by SEED.
 */
public class ITPlugin extends AbstractPlugin {
    public static final String IT_CLASS_NAME = "org.seedstack.seed.it.class.name";
    public static final String DEFAULT_CONFIGURATION_PREFIX = "org.seedstack.seed.it.default-configuration.";

    private static final Logger LOGGER = LoggerFactory.getLogger(ITPlugin.class);

    private final Set<Class<?>> itBindClasses = new HashSet<>();
    private final Set<Class<? extends Module>> itInstallModules = new HashSet<>();
    private InMemoryProvider defaultConfigurationProvider;
    private File temporaryAppStorage;
    private Class<?> testClass;

    @SuppressWarnings("unchecked")
    private final Specification<Class<?>> itBindingSpec = or(classAnnotatedWith(ITBind.class));
    @SuppressWarnings("unchecked")
    private final Specification<Class<?>> itInstallSpec = or(classAnnotatedWith(ITInstall.class));

    @Override
    public String name() {
        return "it";
    }

    @Override
    public Collection<ClasspathScanRequest> classpathScanRequests() {
        return classpathScanRequestBuilder().specification(itBindingSpec).specification(itInstallSpec).build();
    }

    @Override
    public void provideContainerContext(Object containerContext) {
        defaultConfigurationProvider = ((SeedRuntime) containerContext).getDefaultConfiguration();
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public InitState init(InitContext initContext) {
        String itClassName = initContext.kernelParam(IT_CLASS_NAME);

        // Create temporary directory for application storage
        temporaryAppStorage = Files.createTempDir();
        LOGGER.info("Created temporary application storage directory {}", temporaryAppStorage.getAbsolutePath());
        defaultConfigurationProvider.put("org.seedstack.seed.core.storage", temporaryAppStorage.getAbsolutePath());

        // Process default configuration
        for (Map.Entry<String, String> kernelParam : initContext.kernelParams().entrySet()) {
            if (kernelParam.getKey().startsWith(DEFAULT_CONFIGURATION_PREFIX)) {
                defaultConfigurationProvider.put(kernelParam.getKey().substring(DEFAULT_CONFIGURATION_PREFIX.length()), kernelParam.getValue());
            }
        }

        Map<Specification, Collection<Class<?>>> scannedTypesBySpecification = initContext.scannedTypesBySpecification();

        // For @ITBind
        Collection<Class<?>> itBindClassCandidates = scannedTypesBySpecification.get(itBindingSpec);
        if (itBindClassCandidates != null) {
            itBindClasses.addAll(itBindClassCandidates);
        }

        // For @ITInstall
        Collection<Class<?>> itInstallClassCandidates = scannedTypesBySpecification.get(itInstallSpec);
        if (itInstallClassCandidates != null) {
            for (Class<?> itInstallClassCandidate : itInstallClassCandidates) {
                if (Module.class.isAssignableFrom(itInstallClassCandidate)) {
                    itInstallModules.add((Class<? extends Module>) itInstallClassCandidate);
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
            // Clean the temporary application storage
            deleteRecursively(temporaryAppStorage);
            LOGGER.info("Deleted temporary application storage directory {}", temporaryAppStorage.getAbsolutePath());

            // Clean the default configuration system property
        } catch (Exception e) {
            LOGGER.warn("Unable to delete temporary application storage directory " + temporaryAppStorage.getAbsolutePath(), e);
        }
    }

    @Override
    public Object nativeUnitModule() {
        Set<Module> itModules = new HashSet<>();

        for (Class<? extends Module> klazz : itInstallModules) {
            try {
                Constructor<? extends Module> declaredConstructor = klazz.getDeclaredConstructor();
                declaredConstructor.setAccessible(true);
                itModules.add(declaredConstructor.newInstance());
            } catch (Exception e) {
                throw SeedException.wrap(e, ITErrorCode.UNABLE_TO_INSTANTIATE_IT_MODULE).put("moduleClass", klazz.getCanonicalName());
            }
        }

        return new ITModule(testClass, itBindClasses, itModules);
    }

    private void deleteRecursively(final File file) throws IOException {
        if (!file.exists()) {
            return;
        }

        // TODO when building for Java 7 or later, use symlink detection, meanwhile let's hope the temporary IT directory doesn't contain any symlink

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
