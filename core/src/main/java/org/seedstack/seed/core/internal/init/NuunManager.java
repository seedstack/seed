/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.init;

import io.nuun.kernel.api.Kernel;
import io.nuun.kernel.api.config.KernelConfiguration;
import io.nuun.kernel.core.NuunCore;
import io.nuun.kernel.core.internal.scanner.AbstractClasspathScanner;
import org.reflections.vfs.Vfs;
import org.seedstack.seed.core.internal.scan.ClasspathScanHandler;
import org.seedstack.seed.core.internal.scan.FallbackUrlType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public class NuunManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(NuunManager.class);

    private List<Vfs.UrlType> savedUrlTypes;
    private List<Vfs.UrlType> detectedUrlTypes;
    private boolean initialized;

    public synchronized void configure() {
        // Load Nuun and Reflections classes to force initialization of Vfs url types
        try {
            Class.forName(Vfs.class.getCanonicalName());
            Class.forName(AbstractClasspathScanner.class.getCanonicalName());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Cannot initialize the classpath scanning infrastructure", e);
        }

        // Save existing url types
        savedUrlTypes = Vfs.getDefaultUrlTypes();

        // Find all classpath scan handlers and add their Vfs url types
        List<Vfs.UrlType> urlTypes = new ArrayList<Vfs.UrlType>();
        for (ClasspathScanHandler classpathScanHandler : ServiceLoader.load(ClasspathScanHandler.class)) {
            LOGGER.trace("Adding classpath handler {}", classpathScanHandler.getClass().getCanonicalName());
            urlTypes.addAll(classpathScanHandler.urlTypes());
        }
        detectedUrlTypes = urlTypes;

        initialized = true;
    }

    public synchronized Kernel initKernel(KernelConfiguration kernelConfiguration) {
        if (!initialized) {
            throw new IllegalStateException("Nuun is not initialized, cannot initialize a kernel");
        }

        // Kernel instantiation
        Kernel kernel = NuunCore.createKernel(kernelConfiguration);
        FallbackUrlType fallbackUrlType = new FallbackUrlType();
        List<Vfs.UrlType> urlTypes = new ArrayList<Vfs.UrlType>(detectedUrlTypes);
        urlTypes.add(fallbackUrlType);

        LOGGER.debug("Registered URL types for classpath scan: " + urlTypes);

        // Kernel initialization
        Vfs.setDefaultURLTypes(urlTypes);
        kernel.init();
        Vfs.setDefaultURLTypes(savedUrlTypes);

        // Log if any URL were not scanned
        int failedUrlCount = fallbackUrlType.getFailedUrls().size();
        if (failedUrlCount > 0) {
            LOGGER.info("{} URL(s) were not scanned, enable debug logging to see them", failedUrlCount);
            if (LOGGER.isTraceEnabled()) {
                for (URL failedUrl : fallbackUrlType.getFailedUrls()) {
                    LOGGER.debug("URL not scanned: {}", failedUrl);
                }
            }
        }

        return kernel;
    }

    public synchronized void restore() {
        initialized = false;
    }
}
