/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.it.internal;

import com.google.inject.Module;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.PluginException;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.api.plugin.request.ClasspathScanRequest;
import org.kametic.specifications.Specification;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.core.SeedRuntime;
import org.seedstack.seed.core.internal.AbstractSeedPlugin;
import org.seedstack.seed.it.ITBind;
import org.seedstack.seed.it.ITInstall;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.seedstack.shed.reflect.ReflectUtils.makeAccessible;

/**
 * This plugin automatically enable integration tests to be managed by SEED.
 */
public class ITPlugin extends AbstractSeedPlugin {
    public static final String IT_CLASS_NAME = "seedstack.it.className";
    private static final Logger LOGGER = LoggerFactory.getLogger(ITPlugin.class);

    private final Set<Class<?>> itBindClasses = new HashSet<>();
    private final Set<Class<? extends Module>> itInstallModules = new HashSet<>();
    private File temporaryAppStorage;
    private Class<?> testClass;

    @SuppressWarnings("unchecked")
    private final Specification<Class<?>> itBindingSpec = or(classAnnotatedWith(ITBind.class));
    @SuppressWarnings("unchecked")
    private final Specification<Class<?>> itInstallSpec = or(classAnnotatedWith(ITInstall.class));

    @Override
    public String name() {
        return "testing";
    }

    @Override
    public Collection<ClasspathScanRequest> classpathScanRequests() {
        return classpathScanRequestBuilder().specification(itBindingSpec).specification(itInstallSpec).build();
    }

    @Override
    public void setup(SeedRuntime seedRuntime) {
        // Create temporary directory for application storage
        try {
            temporaryAppStorage = Files.createTempDirectory("seedstack-it-").toFile();
            LOGGER.info("Created temporary application storage directory {}", temporaryAppStorage.getAbsolutePath());
            seedRuntime.setDefaultConfiguration("application.storage", temporaryAppStorage.getAbsolutePath());
        } catch (IOException e) {
            LOGGER.warn("Unable to create temporary application storage directory");
        }
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public InitState initialize(InitContext initContext) {
        String itClassName = initContext.kernelParam(IT_CLASS_NAME);
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
            if (temporaryAppStorage != null) {
                deleteRecursively(temporaryAppStorage);
                LOGGER.info("Deleted temporary application storage directory {}", temporaryAppStorage.getAbsolutePath());
            }
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
                makeAccessible(declaredConstructor);
                itModules.add(declaredConstructor.newInstance());
            } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
                throw SeedException.wrap(e, ITErrorCode.UNABLE_TO_INSTANTIATE_IT_MODULE).put("moduleClass", klazz.getCanonicalName());
            }
        }

        return new ITModule(testClass, itBindClasses, itModules);
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
            LOGGER.debug("Unable to delete file {}" + file.getAbsolutePath());
        }
    }
}
