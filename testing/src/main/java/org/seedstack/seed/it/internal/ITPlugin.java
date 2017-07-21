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
import org.seedstack.seed.core.SeedRuntime;
import org.seedstack.seed.core.internal.AbstractSeedPlugin;
import org.seedstack.seed.core.internal.BindingDefinition;
import org.seedstack.seed.core.internal.utils.SpecificationBuilder;
import org.seedstack.shed.reflect.Classes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.file.Files;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * This plugin automatically enable integration tests to be managed by SEED.
 */
public class ITPlugin extends AbstractSeedPlugin {
    public static final String IT_CLASS_NAME = "seedstack.it.className";
    private static final Logger LOGGER = LoggerFactory.getLogger(ITPlugin.class);
    private static final Specification<Class<?>> installSpecification = new SpecificationBuilder<>(ITInstallResolver.INSTANCE).build();
    private static final Specification<Class<?>> bindSpecification = new SpecificationBuilder<>(ITBindResolver.INSTANCE).build();
    private final Set<Class<? extends Module>> modules = new HashSet<>();
    private final Set<Class<? extends Module>> overridingModules = new HashSet<>();
    private final Set<BindingDefinition> bindings = new HashSet<>();
    private final Set<BindingDefinition> overridingBindings = new HashSet<>();
    private File temporaryAppStorage;
    private Class<?> testClass;

    @Override
    public String name() {
        return "testing";
    }

    @Override
    public Collection<ClasspathScanRequest> classpathScanRequests() {
        return classpathScanRequestBuilder()
                .specification(installSpecification)
                .specification(bindSpecification)
                .build();
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
    public InitState initialize(InitContext initContext) {
        String itClassName = initContext.kernelParam(IT_CLASS_NAME);

        detectModules(initContext);
        detectBindings(initContext);

        if (itClassName != null) {
            try {
                testClass = Class.forName(itClassName);
            } catch (ClassNotFoundException e) {
                throw new PluginException("Unable to load the test class " + itClassName, e);
            }
        }

        return InitState.INITIALIZED;
    }

    @SuppressWarnings("unchecked")
    private void detectModules(InitContext initContext) {
        initContext.scannedTypesBySpecification().get(installSpecification)
                .stream()
                .filter(Module.class::isAssignableFrom)
                .forEach(candidate -> ITInstallResolver.INSTANCE.apply(candidate).ifPresent(annotation -> {
                    if (annotation.override()) {
                        overridingModules.add((Class<? extends Module>) candidate);
                    } else {
                        modules.add((Class<? extends Module>) candidate);
                    }
                }));
    }

    @SuppressWarnings("unchecked")
    private void detectBindings(InitContext initContext) {
        initContext.scannedTypesBySpecification().get(bindSpecification)
                .forEach(candidate -> ITBindResolver.INSTANCE.apply(candidate).ifPresent(annotation -> {
                    if (annotation.override()) {
                        overridingBindings.add(new BindingDefinition<>(
                                (Class<Object>) (annotation.from() == Object.class ? null : annotation.from()),
                                (annotation.annotated() == Annotation.class ? null : annotation.annotated()),
                                isNullOrEmpty(annotation.named()) ? null : annotation.named(),
                                candidate
                        ));
                    } else {
                        bindings.add(new BindingDefinition<>(
                                (Class<Object>) (annotation.from() == Object.class ? null : annotation.from()),
                                (annotation.annotated() == Annotation.class ? null : annotation.annotated()),
                                isNullOrEmpty(annotation.named()) ? null : annotation.named(),
                                candidate
                        ));
                    }
                }));
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
        return new ITModule(
                testClass,
                modules.stream().map(Classes::instantiateDefault).collect(Collectors.toSet()),
                bindings,
                false
        );
    }

    @Override
    public Object nativeOverridingUnitModule() {
        return new ITModule(
                null,
                overridingModules.stream().map(Classes::instantiateDefault).collect(Collectors.toSet()),
                overridingBindings,
                true
        );
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
