/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
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
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;
import org.reflections.vfs.Vfs;
import org.seedstack.seed.core.api.DiagnosticManager;
import org.seedstack.seed.core.api.Install;
import org.seedstack.seed.core.api.SeedException;
import org.seedstack.seed.core.spi.diagnostic.DiagnosticDomain;
import org.seedstack.seed.core.spi.diagnostic.DiagnosticInfoCollector;
import org.seedstack.seed.core.utils.SeedReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.*;

/**
 * Core plugin that setup common SEED package roots and scans modules to install via the
 * {@link Install} annotation.
 *
 * @author adrien.lauer@mpsa.com
 */
public class CorePlugin extends AbstractPlugin {
    public static final String SEED_PACKAGE_ROOT = "org.seedstack";
    public static final String CORE_PLUGIN_PREFIX = "org.seedstack.seed.core";
    public static final String DETAILS_MESSAGE = "Details of the previous error below";

    private static final String SEED_BOOTSTRAP_PATH = "META-INF/seed-bootstrap.properties";
    private static final Logger LOGGER = LoggerFactory.getLogger(CorePlugin.class);

    private static final DiagnosticManagerImpl DIAGNOSTIC_MANAGER = new DiagnosticManagerImpl();

    static {
        Vfs.addDefaultURLTypes(new Vfs.UrlType() {
            @Override
            public boolean matches(URL url) {
                return "jndi".equals(url.getProtocol()) && url.toExternalForm().contains(".jar");
            }

            @Override
            public Vfs.Dir createDir(final URL url) {
                return new JndiJarInputDir(url);
            }
        });

        Vfs.addDefaultURLTypes(new Vfs.UrlType() {
            @Override
            public boolean matches(URL url) {
                return "jndi".equals(url.getProtocol()) && !url.toExternalForm().contains(".jar");
            }

            @Override
            public Vfs.Dir createDir(final URL url) {
                return new JndiInputDir(url);
            }
        });

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                DIAGNOSTIC_MANAGER.dumpDiagnosticReport(e);
                System.err.print("Exception in thread \"" + t.getName() + "\" ");
                e.printStackTrace(System.err);
            }
        });
    }

    private final Configuration bootstrapConfiguration;
    private final Set<Class<? extends Module>> seedModules = new HashSet<Class<? extends Module>>();
    private final Map<String, Class<? extends DiagnosticInfoCollector>> diagnosticInfoCollectors = new HashMap<String, Class<? extends DiagnosticInfoCollector>>();

    /**
     * @return the diagnostic manager singleton.
     */
    public static DiagnosticManager getDiagnosticManager() {
        return DIAGNOSTIC_MANAGER;
    }

    public CorePlugin() {
        bootstrapConfiguration = loadBootstrapConfiguration();
    }

    @Override
    public String name() {
        return "seed-core-plugin";
    }

    @SuppressWarnings("unchecked")
    @Override
    public InitState init(InitContext initContext) {
        Set<URL> scannedUrls = extractScannedUrls(initContext);
        if (scannedUrls != null) {
            DIAGNOSTIC_MANAGER.setClasspathUrls(scannedUrls);
        }

        Map<Class<? extends Annotation>, Collection<Class<?>>> scannedClassesByAnnotationClass = initContext.scannedClassesByAnnotationClass();
        Map<Class<?>, Collection<Class<?>>> scannedSubTypesByParentClass = initContext.scannedSubTypesByParentClass();

        for (Class<?> candidate : scannedSubTypesByParentClass.get(DiagnosticInfoCollector.class)) {
            if (DiagnosticInfoCollector.class.isAssignableFrom(candidate)) {
                DiagnosticDomain diagnosticDomain = candidate.getAnnotation(DiagnosticDomain.class);
                if (diagnosticDomain != null) {
                    diagnosticInfoCollectors.put(diagnosticDomain.value(), (Class<? extends DiagnosticInfoCollector>) candidate);
                    LOGGER.trace("Detected diagnostic collector {} for diagnostic domain {}", candidate.getCanonicalName(), diagnosticDomain.value());
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
        return classpathScanRequestBuilder().subtypeOf(DiagnosticInfoCollector.class).annotationType(Install.class).build();
    }

    @Override
    public String pluginPackageRoot() {
        String packageRoots = SEED_PACKAGE_ROOT;
        String[] applicationPackageRoots = bootstrapConfiguration.getStringArray("package-roots"); // expect package prefixes joined by a comma (",")
        if (applicationPackageRoots == null || applicationPackageRoots.length == 0) {
            LOGGER.warn("No additional package roots specified. SEED will only scan " + packageRoots);
        } else {
            packageRoots += "," + StringUtils.join(applicationPackageRoots, ",");
        }
        return packageRoots;
    }

    private Configuration loadBootstrapConfiguration() {
        ClassLoader classLoader = SeedReflectionUtils.findMostCompleteClassLoader();
        if (classLoader == null) {
            throw SeedException.createNew(CoreErrorCode.UNABLE_TO_FIND_CLASSLOADER);
        }

        PropertiesConfiguration configuration = new PropertiesConfiguration();

        try {
            Enumeration<URL> urls = classLoader.getResources(SEED_BOOTSTRAP_PATH);

            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                configuration.load(url);
            }
        } catch (Exception e) {
            throw SeedException.wrap(e, CoreErrorCode.UNEXPECTED_EXCEPTION);
        }

        return configuration;
    }

    /**
     * Returns the configuration coming from the SEED bootstrap properties.
     *
     * <strong>This configuration should not be mutated.</strong>
     *
     * @return bootstrap configuration
     */
    public Configuration getBootstrapConfiguration() {
        return bootstrapConfiguration;
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

        return new CoreModule(subModules, DIAGNOSTIC_MANAGER, diagnosticInfoCollectors);
    }

    @SuppressWarnings("unchecked")
    private Set<URL> extractScannedUrls(InitContext initContext) {
        try {
            return new HashSet<URL>((Set<URL>) SeedReflectionUtils.invokeMethod(SeedReflectionUtils.getFieldValue(initContext, "classpathScanner"), "computeUrls"));
        } catch (Exception e) {
            LOGGER.warn("Unable to collect scanned classpath");
            LOGGER.debug(DETAILS_MESSAGE, e);
        }

        return null;
    }
}
