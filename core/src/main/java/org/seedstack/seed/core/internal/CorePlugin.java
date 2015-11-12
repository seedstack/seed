/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal;

import com.google.inject.Module;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.PluginException;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.api.plugin.request.ClasspathScanRequest;
import io.nuun.kernel.core.AbstractPlugin;
import io.nuun.kernel.core.internal.scanner.AbstractClasspathScanner;
import org.reflections.Reflections;
import org.reflections.vfs.Vfs;
import org.seedstack.seed.CoreErrorCode;
import org.seedstack.seed.DiagnosticManager;
import org.seedstack.seed.Install;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.core.internal.scan.ClasspathScanHandler;
import org.seedstack.seed.core.internal.scan.FallbackUrlType;
import org.seedstack.seed.core.utils.SeedReflectionUtils;
import org.seedstack.seed.spi.dependency.DependencyProvider;
import org.seedstack.seed.spi.dependency.Maybe;
import org.seedstack.seed.spi.diagnostic.DiagnosticDomain;
import org.seedstack.seed.spi.diagnostic.DiagnosticInfoCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

/**
 * Core plugin that setup common SEED package roots and scans modules to install via the
 * {@link Install} annotation.
 *
 * @author adrien.lauer@mpsa.com
 */
public class CorePlugin extends AbstractPlugin {
    public static final String SEEDSTACK_PACKAGE_ROOT = "org.seedstack";
    public static final String CORE_PLUGIN_PREFIX = "org.seedstack.seed.core";
    public static final String DETAILS_MESSAGE = "Details of the previous error below";
    private static final Logger LOGGER = LoggerFactory.getLogger(CorePlugin.class);

    private static final DiagnosticManagerImpl DIAGNOSTIC_MANAGER = new DiagnosticManagerImpl();

    private static final FallbackUrlType FALLBACK_URL_TYPE = new FallbackUrlType();

    static {
        // Disable Reflections logs
        Reflections.log = null;

        // Load Nuun and Reflections classes to force initialization of Vfs url types
        try {
            Class.forName(Vfs.class.getCanonicalName());
            Class.forName(AbstractClasspathScanner.class.getCanonicalName());
        } catch (ClassNotFoundException e) {
            throw new PluginException("Cannot initialize the classpath scanning infrastructure", e);
        }

        // Find all classpath scan handlers and add their Vfs url types
        List<Vfs.UrlType> urlTypes = new ArrayList<Vfs.UrlType>();
        for (ClasspathScanHandler classpathScanHandler : ServiceLoader.load(ClasspathScanHandler.class)) {
            LOGGER.trace("Adding classpath handler {}", classpathScanHandler.getClass().getCanonicalName());
            urlTypes.addAll(classpathScanHandler.urlTypes());
        }

        // Add fallback Vfs url type
        urlTypes.add(FALLBACK_URL_TYPE);

        // Overwrites all url types to Vfs
        Vfs.setDefaultURLTypes(urlTypes);

        // Default uncaught exception handler
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                DIAGNOSTIC_MANAGER.dumpDiagnosticReport(e);
                System.err.print("Exception in thread \"" + t.getName() + "\" ");
                e.printStackTrace(System.err);
            }
        });
    }

    private final Set<Class<? extends Module>> seedModules = new HashSet<Class<? extends Module>>();
    private final Map<String, DiagnosticInfoCollector> diagnosticInfoCollectors = new HashMap<String, DiagnosticInfoCollector>();
    private Map<Class<?>, Maybe<? extends DependencyProvider>> optionalDependencies = new HashMap<Class<?>, Maybe<? extends DependencyProvider>>();

    /**
     * @return the diagnostic manager singleton.
     */
    public static DiagnosticManager getDiagnosticManager() {
        return DIAGNOSTIC_MANAGER;
    }

    @Override
    public String name() {
        return "seed-core-plugin";
    }

    @Override
    public String pluginPackageRoot() {
        return SEEDSTACK_PACKAGE_ROOT;
    }

    @SuppressWarnings("unchecked")
    @Override
    public InitState init(InitContext initContext) {
    	
        int failedUrlCount = FALLBACK_URL_TYPE.getFailedUrls().size();
        if (failedUrlCount > 0) {
            LOGGER.info("{} URL(s) were not scanned, enable debug logging to see them", failedUrlCount);
            if (LOGGER.isTraceEnabled()) {
                for (URL failedUrl : FALLBACK_URL_TYPE.getFailedUrls()) {
                    LOGGER.debug("URL not scanned: {}", failedUrl);
                }
            }
        }

        Set<URL> scannedUrls = extractScannedUrls(initContext);
        if (scannedUrls != null) {
            DIAGNOSTIC_MANAGER.setClasspathUrls(scannedUrls);
        }

        Map<Class<? extends Annotation>, Collection<Class<?>>> scannedClassesByAnnotationClass = initContext.scannedClassesByAnnotationClass();
        Map<Class<?>, Collection<Class<?>>> scannedSubTypesByParentClass = initContext.scannedSubTypesByParentClass();

        // Scan optional dependencies
        for (Class<?> candidate : scannedSubTypesByParentClass.get(DependencyProvider.class)) {
            getDependency((Class<DependencyProvider>)candidate);
        }

        for (Class<?> candidate : scannedSubTypesByParentClass.get(DiagnosticInfoCollector.class)) {
            if (DiagnosticInfoCollector.class.isAssignableFrom(candidate)) {
                DiagnosticDomain diagnosticDomain = candidate.getAnnotation(DiagnosticDomain.class);
                if (diagnosticDomain != null) {
                    try {
                        diagnosticInfoCollectors.put(diagnosticDomain.value(), (DiagnosticInfoCollector) candidate.newInstance());
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
    public Collection<ClasspathScanRequest> classpathScanRequests() {
        return classpathScanRequestBuilder().subtypeOf(DiagnosticInfoCollector.class).annotationType(Install.class).subtypeOf(DependencyProvider.class).build();
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

        return new CoreModule(subModules, DIAGNOSTIC_MANAGER, diagnosticInfoCollectors, this.optionalDependencies);
    }

    /**
     * Return {@link Maybe} which contains the provider if dependency is present.
     * Always return a {@link Maybe} instance.
     *
     * @param providerClass provider to use an optional dependency
     * @return {@link Maybe} which contains the provider if dependency is present
     */
    @SuppressWarnings("unchecked")
    public <T extends DependencyProvider>  Maybe<T> getDependency(Class<T> providerClass) {
        if (! optionalDependencies.containsKey(providerClass)) {
            Maybe<T> maybe = new Maybe<T>(null);
            try {
                T provider = providerClass.newInstance();
                if (SeedReflectionUtils.forName(provider.getClassToCheck()).isPresent()) {
                    LOGGER.debug("Found a new optional provider [{}} for [{}]",providerClass.getName(),provider.getClassToCheck());
                    maybe = new Maybe<T>(provider);
                }
            } catch (Exception e) {
                throw SeedException.wrap(e, CoreErrorCode.UNABLE_TO_INSTANTIATE_CLASS).put("class", providerClass.getCanonicalName());
            }
            optionalDependencies.put(providerClass, maybe);
        }
        return (Maybe<T>)optionalDependencies.get(providerClass);
    }

    @SuppressWarnings("unchecked")
    private Set<URL> extractScannedUrls(InitContext initContext) {
        try {
            return new HashSet<URL>((Set<URL>) SeedReflectionUtils.invokeMethod(SeedReflectionUtils.getFieldValue(unproxify(initContext), "classpathScanner"), "computeUrls"));
        } catch (Exception e) {
            LOGGER.warn("Unable to collect scanned classpath");
            LOGGER.debug(DETAILS_MESSAGE, e);
        }

        return null;
    }

    // TODO remove this when not needed anymore (see at call site)
    private InitContext unproxify(InitContext initContext) throws Exception {
        InvocationHandler invocationHandler;
        try {
            invocationHandler = Proxy.getInvocationHandler(initContext);
        } catch(IllegalArgumentException e) {
            // not a proxy
            return initContext;
        }
        Field field = invocationHandler.getClass().getDeclaredField("val$initContext");
        field.setAccessible(true);
        return (InitContext) field.get(invocationHandler);
    }
}
