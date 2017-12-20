/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
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
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.kametic.specifications.Specification;
import org.seedstack.seed.core.internal.utils.SpecificationBuilder;
import org.seedstack.shed.reflect.Classes;

/**
 * Core plugin that configures base package roots and detects diagnostic collectors, dependency providers, Guice modules
 * and configuration files.
 */
public class CorePlugin extends AbstractSeedPlugin {
    static final String AUTODETECT_MODULES_KERNEL_PARAM = "seedstack.autodetectModules";
    static final String AUTODETECT_BINDINGS_KERNEL_PARAM = "seedstack.autodetectBindings";
    private static final String SEEDSTACK_PACKAGE = "org.seedstack";
    private static final Specification<Class<?>> installSpecification = new SpecificationBuilder<>(
            InstallResolver.INSTANCE).build();
    private static final Specification<Class<?>> bindSpecification = new SpecificationBuilder<>(
            BindResolver.INSTANCE).build();
    private final Set<Class<? extends Module>> modules = new HashSet<>();
    private final Set<Class<? extends Module>> overridingModules = new HashSet<>();
    private final Set<BindingDefinition> bindings = new HashSet<>();
    private final Set<BindingDefinition> overridingBindings = new HashSet<>();

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
                .specification(installSpecification)
                .specification(bindSpecification)
                .build();
    }

    @SuppressWarnings("unchecked")
    @Override
    public InitState initialize(InitContext initContext) {
        String autodetectModules = initContext.kernelParam(AUTODETECT_MODULES_KERNEL_PARAM);
        if (Strings.isNullOrEmpty(autodetectModules) || Boolean.parseBoolean(autodetectModules)) {
            detectModules(initContext);
        }
        String autodetectBindings = initContext.kernelParam(AUTODETECT_BINDINGS_KERNEL_PARAM);
        if (Strings.isNullOrEmpty(autodetectBindings) || Boolean.parseBoolean(autodetectBindings)) {
            detectBindings(initContext);
        }
        return InitState.INITIALIZED;
    }

    @SuppressWarnings("unchecked")
    private void detectModules(InitContext initContext) {
        initContext.scannedTypesBySpecification().get(installSpecification)
                .stream()
                .filter(Module.class::isAssignableFrom)
                .forEach(candidate -> InstallResolver.INSTANCE.apply(candidate).ifPresent(annotation -> {
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
                .forEach(candidate -> BindResolver.INSTANCE.apply(candidate).ifPresent(annotation -> {
                    if (annotation.override()) {
                        overridingBindings.add(new BindingDefinition<>(
                                candidate,
                                (Class<Object>) (annotation.from() == Object.class ? null : annotation.from())
                        ));
                    } else {
                        bindings.add(new BindingDefinition<>(
                                candidate,
                                (Class<Object>) (annotation.from() == Object.class ? null : annotation.from())
                        ));
                    }
                }));
    }

    @Override
    public Object nativeUnitModule() {
        return new CoreModule(
                modules.stream().map(Classes::instantiateDefault).collect(Collectors.toSet()),
                bindings,
                false
        );
    }

    @Override
    public Object nativeOverridingUnitModule() {
        return new CoreModule(
                overridingModules.stream().map(Classes::instantiateDefault).collect(Collectors.toSet()),
                overridingBindings,
                true
        );
    }
}
