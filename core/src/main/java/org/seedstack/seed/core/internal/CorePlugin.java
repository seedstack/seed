/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal;

import com.google.inject.Module;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.api.plugin.request.ClasspathScanRequest;
import org.seedstack.seed.Install;
import org.seedstack.seed.core.utils.SeedReflectionUtils;
import org.seedstack.seed.spi.dependency.DependencyProvider;
import org.seedstack.seed.SeedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Core plugin that configures base package roots and detects diagnostic collectors, dependency providers, Guice modules
 * and configuration files.
 *
 * @author adrien.lauer@mpsa.com
 */
public class CorePlugin extends AbstractSeedPlugin {
    private static final Logger LOGGER = LoggerFactory.getLogger(CorePlugin.class);
    private static final String SEEDSTACK_PACKAGE = "org.seedstack";
    private final Set<Class<? extends Module>> seedModules = new HashSet<>();
    private final Map<Class<?>, Optional<? extends DependencyProvider>> optionalDependencies = new HashMap<>();

    @Override
    public String name() {
        return "core";
    }

    @Override
    public String pluginPackageRoot() {
        return SEEDSTACK_PACKAGE;
    }

    @Override
    public Collection<ClasspathScanRequest> classpathScanRequests() {
        return classpathScanRequestBuilder()
                .subtypeOf(DependencyProvider.class)
                .annotationType(Install.class)
                .build();
    }

    @SuppressWarnings("unchecked")
    @Override
    public InitState initialize(InitContext initContext) {
        // Scan optional dependencies
        detectDependencyProviders(initContext);

        // Detect modules to install
        detectModules(initContext);

        return InitState.INITIALIZED;
    }

    @SuppressWarnings("unchecked")
    private void detectModules(InitContext initContext) {
        initContext.scannedClassesByAnnotationClass().get(Install.class)
                .stream()
                .filter(Module.class::isAssignableFrom)
                .forEach(candidate -> {
                    seedModules.add((Class<Module>) candidate);
                    LOGGER.trace("Detected module to install {}", candidate.getCanonicalName());
                });
        LOGGER.debug("Detected {} module(s) to install", seedModules.size());
    }

    @SuppressWarnings("unchecked")
    private void detectDependencyProviders(InitContext initContext) {
        initContext.scannedSubTypesByParentClass().get(DependencyProvider.class)
                .stream()
                .filter(DependencyProvider.class::isAssignableFrom)
                .forEach(candidate -> getDependency((Class<DependencyProvider>) candidate));
    }

    @Override
    public Object nativeUnitModule() {
        Collection<Module> subModules = new HashSet<>();

        for (Class<? extends Module> klazz : seedModules) {
            try {
                Constructor<? extends Module> declaredConstructor = klazz.getDeclaredConstructor();
                declaredConstructor.setAccessible(true);
                subModules.add(declaredConstructor.newInstance());
            } catch (Exception e) {
                throw SeedException.wrap(e, CoreErrorCode.UNABLE_TO_INSTANTIATE_MODULE).put("module", klazz.getCanonicalName());
            }
        }

        return new CoreModule(subModules, optionalDependencies);
    }

    /**
     * Return {@link Optional} which contains the provider if dependency is present.
     * Always return a {@link Optional} instance.
     *
     * @param providerClass provider to use an optional dependency
     * @return {@link Optional} which contains the provider if dependency is present
     */
    @SuppressWarnings("unchecked")
    public <T extends DependencyProvider> Optional<T> getDependency(Class<T> providerClass) {
        if (!optionalDependencies.containsKey(providerClass)) {
            Optional<T> optionalDependency = Optional.empty();
            try {
                T provider = providerClass.newInstance();
                if (SeedReflectionUtils.optionalOfClass(provider.getClassToCheck()).isPresent()) {
                    LOGGER.debug("Found a new optional provider [{}] for [{}]", providerClass.getName(), provider.getClassToCheck());
                    optionalDependency = Optional.of(provider);
                }
            } catch (Exception e) {
                throw SeedException.wrap(e, CoreErrorCode.UNABLE_TO_INSTANTIATE_CLASS).put("class", providerClass.getCanonicalName());
            }
            optionalDependencies.put(providerClass, optionalDependency);
        }
        return (Optional<T>) optionalDependencies.get(providerClass);
    }
}
