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
import io.nuun.kernel.core.AbstractPlugin;
import org.seedstack.seed.DiagnosticManager;
import org.seedstack.seed.Install;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.SeedRuntime;
import org.seedstack.seed.core.utils.SeedReflectionUtils;
import org.seedstack.seed.spi.dependency.DependencyProvider;
import org.seedstack.seed.spi.dependency.Maybe;
import org.seedstack.seed.spi.diagnostic.DiagnosticDomain;
import org.seedstack.seed.spi.diagnostic.DiagnosticInfoCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Core plugin that setup common Seed package roots and scans modules to install via the
 * {@link Install} annotation.
 *
 * @author adrien.lauer@mpsa.com
 */
public class CorePlugin extends AbstractPlugin {
    public static final String SEEDSTACK_PACKAGE_ROOT = "org.seedstack";
    public static final String CORE_PLUGIN_PREFIX = "org.seedstack.seed.core";
    private static final Logger LOGGER = LoggerFactory.getLogger(CorePlugin.class);

    private final Set<Class<? extends Module>> seedModules = new HashSet<Class<? extends Module>>();
    private final Map<String, DiagnosticInfoCollector> diagnosticInfoCollectors = new HashMap<String, DiagnosticInfoCollector>();
    private final Map<Class<?>, Maybe<? extends DependencyProvider>> optionalDependencies = new HashMap<Class<?>, Maybe<? extends DependencyProvider>>();
    private DiagnosticManager diagnosticManager;

    @Override
    public String name() {
        return "core";
    }

    @Override
    public void provideContainerContext(Object containerContext) {
        diagnosticManager = ((SeedRuntime) containerContext).getDiagnosticManager();
    }

    @Override
    public String pluginPackageRoot() {
        return SEEDSTACK_PACKAGE_ROOT;
    }

    @Override
    public Collection<ClasspathScanRequest> classpathScanRequests() {
        return classpathScanRequestBuilder()
                .subtypeOf(DiagnosticInfoCollector.class)
                .annotationType(Install.class)
                .subtypeOf(DependencyProvider.class)
                .build();
    }

    @SuppressWarnings("unchecked")
    @Override
    public InitState init(InitContext initContext) {
        registerDiagnosticCollector("core", new CoreDiagnosticCollector(initContext));

        Map<Class<? extends Annotation>, Collection<Class<?>>> scannedClassesByAnnotationClass = initContext.scannedClassesByAnnotationClass();
        Map<Class<?>, Collection<Class<?>>> scannedSubTypesByParentClass = initContext.scannedSubTypesByParentClass();

        // Scan optional dependencies
        for (Class<?> candidate : scannedSubTypesByParentClass.get(DependencyProvider.class)) {
            getDependency((Class<DependencyProvider>) candidate);
        }

        for (Class<?> candidate : scannedSubTypesByParentClass.get(DiagnosticInfoCollector.class)) {
            if (DiagnosticInfoCollector.class.isAssignableFrom(candidate)) {
                DiagnosticDomain diagnosticDomain = candidate.getAnnotation(DiagnosticDomain.class);
                if (diagnosticDomain != null) {
                    try {
                        registerDiagnosticCollector(diagnosticDomain.value(), (DiagnosticInfoCollector) candidate.newInstance());
                        LOGGER.trace("Detected diagnostic collector {} for diagnostic domain {}", candidate.getCanonicalName(), diagnosticDomain.value());
                    } catch (Exception e) {
                        throw SeedException.wrap(e, CoreErrorCode.UNABLE_TO_CREATE_DIAGNOSTIC_COLLECTOR).put("diagnosticCollectorClass", candidate.getClass().getCanonicalName());
                    }
                }
            }
        }
        LOGGER.debug("Detected {} diagnostic collector(s)", diagnosticInfoCollectors.size());

        for (Class<?> candidate : scannedClassesByAnnotationClass.get(Install.class)) {
            if (Module.class.isAssignableFrom(candidate)) {
                seedModules.add(Module.class.getClass().cast(candidate));
                LOGGER.trace("Detected module to install {}", candidate.getCanonicalName());
            }
        }
        LOGGER.debug("Detected {} module(s) to install", seedModules.size());

        return InitState.INITIALIZED;
    }

    @Override
    public Object nativeUnitModule() {
        Collection<Module> subModules = new HashSet<Module>();

        for (Class<? extends Module> klazz : seedModules) {
            try {
                Constructor<? extends Module> declaredConstructor = klazz.getDeclaredConstructor();
                declaredConstructor.setAccessible(true);
                subModules.add(declaredConstructor.newInstance());
            } catch (Exception e) {
                throw SeedException.wrap(e, CoreErrorCode.UNABLE_TO_INSTANTIATE_MODULE).put("module", klazz.getCanonicalName());
            }
        }

        return new CoreModule(subModules, diagnosticManager, diagnosticInfoCollectors, this.optionalDependencies);
    }

    /**
     * This method registers an existing {@link DiagnosticInfoCollector} instance.
     *
     * @param diagnosticDomain        the diagnostic domain name the collector is related to.
     * @param diagnosticInfoCollector the diagnostic collector instance.
     */
    public void registerDiagnosticCollector(String diagnosticDomain, DiagnosticInfoCollector diagnosticInfoCollector) {
        diagnosticInfoCollectors.put(diagnosticDomain, diagnosticInfoCollector);
    }

    /**
     * Return {@link Maybe} which contains the provider if dependency is present.
     * Always return a {@link Maybe} instance.
     *
     * @param providerClass provider to use an optional dependency
     * @return {@link Maybe} which contains the provider if dependency is present
     */
    @SuppressWarnings("unchecked")
    public <T extends DependencyProvider> Maybe<T> getDependency(Class<T> providerClass) {
        if (!optionalDependencies.containsKey(providerClass)) {
            Maybe<T> maybe = new Maybe<T>(null);
            try {
                T provider = providerClass.newInstance();
                if (SeedReflectionUtils.forName(provider.getClassToCheck()).isPresent()) {
                    LOGGER.debug("Found a new optional provider [{}] for [{}]", providerClass.getName(), provider.getClassToCheck());
                    maybe = new Maybe<T>(provider);
                }
            } catch (Exception e) {
                throw SeedException.wrap(e, CoreErrorCode.UNABLE_TO_INSTANTIATE_CLASS).put("class", providerClass.getCanonicalName());
            }
            optionalDependencies.put(providerClass, maybe);
        }
        return (Maybe<T>) optionalDependencies.get(providerClass);
    }
}
