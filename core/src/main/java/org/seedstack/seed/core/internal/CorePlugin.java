/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal;

import com.google.common.base.Strings;
import com.google.inject.Module;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.api.plugin.request.ClasspathScanRequest;
import org.seedstack.seed.Install;
import org.seedstack.seed.SeedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.seedstack.shed.reflect.ReflectUtils.makeAccessible;

/**
 * Core plugin that configures base package roots and detects diagnostic collectors, dependency providers, Guice modules
 * and configuration files.
 */
public class CorePlugin extends AbstractSeedPlugin {
    static final String AUTODETECT_MODULES_KERNEL_PARAM = "seedstack.autodetectModules";
    private static final Logger LOGGER = LoggerFactory.getLogger(CorePlugin.class);
    private static final String SEEDSTACK_PACKAGE = "org.seedstack";
    private final Set<Class<? extends Module>> seedModules = new HashSet<>();
    private final Set<Class<? extends Module>> seedOverridingModules = new HashSet<>();

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
                .annotationType(Install.class)
                .build();
    }

    @SuppressWarnings("unchecked")
    @Override
    public InitState initialize(InitContext initContext) {
        String autodetectModules = initContext.kernelParam(AUTODETECT_MODULES_KERNEL_PARAM);
        if (Strings.isNullOrEmpty(autodetectModules) || Boolean.parseBoolean(autodetectModules)) {
            detectModules(initContext);
        }

        return InitState.INITIALIZED;
    }

    @SuppressWarnings("unchecked")
    private void detectModules(InitContext initContext) {
        initContext.scannedClassesByAnnotationClass().get(Install.class)
                .stream()
                .filter(Module.class::isAssignableFrom)
                .forEach(candidate -> {
                    Install annotation = candidate.getAnnotation(Install.class);
                    if (annotation.override()) {
                        seedOverridingModules.add((Class<Module>) candidate);
                        LOGGER.trace("Detected overriding module to install {}", candidate.getCanonicalName());
                    } else {
                        seedModules.add((Class<Module>) candidate);
                        LOGGER.trace("Detected module to install {}", candidate.getCanonicalName());
                    }
                });
        LOGGER.debug("Detected {} module(s) to install", seedModules.size());
        LOGGER.debug("Detected {} overriding module(s) to install", seedOverridingModules.size());
    }

    @Override
    public Object nativeUnitModule() {
        return new CoreModule(instantiateModules(seedModules));
    }

    @Override
    public Object nativeOverridingUnitModule() {
        return new CoreModule(instantiateModules(seedOverridingModules));
    }

    private Collection<Module> instantiateModules(Collection<Class<? extends Module>> moduleClasses) {
        Collection<Module> modules = new HashSet<>();
        for (Class<? extends Module> moduleClass : moduleClasses) {
            try {
                Constructor<? extends Module> declaredConstructor = moduleClass.getDeclaredConstructor();
                makeAccessible(declaredConstructor);
                modules.add(declaredConstructor.newInstance());
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw SeedException.wrap(e, CoreErrorCode.UNABLE_TO_INSTANTIATE_MODULE).put("module", moduleClass.getCanonicalName());
            }
        }
        return modules;
    }
}
