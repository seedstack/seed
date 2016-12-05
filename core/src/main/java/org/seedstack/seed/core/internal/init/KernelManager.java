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
import org.seedstack.seed.DiagnosticManager;
import org.seedstack.seed.core.Seed;
import org.seedstack.seed.core.SeedRuntime;
import org.seedstack.seed.core.internal.scan.ClasspathScanHandler;
import org.seedstack.seed.core.internal.scan.FallbackUrlType;
import org.seedstack.shed.ClassLoaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.ServiceLoader;

public class KernelManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(KernelManager.class);
    private final List<Vfs.UrlType> savedUrlTypes;
    private final List<Vfs.UrlType> detectedUrlTypes;

    private static class Holder {
        private static final KernelManager INSTANCE = new KernelManager();
    }

    public static KernelManager get() {
        return Holder.INSTANCE;
    }

    private KernelManager() {
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
        List<Vfs.UrlType> urlTypes = new ArrayList<>();
        for (ClasspathScanHandler classpathScanHandler : ServiceLoader.load(ClasspathScanHandler.class)) {
            LOGGER.trace("Detected classpath handler {}", classpathScanHandler.getClass().getCanonicalName());
            urlTypes.addAll(classpathScanHandler.urlTypes());
        }
        LOGGER.debug("URL types for scanning: {}", urlTypes);
        detectedUrlTypes = urlTypes;
    }

    public Kernel createKernel(SeedRuntime seedRuntime, @Nullable KernelConfiguration kernelConfiguration, boolean autoStart) {
        // Startup message
        StringBuilder startMessage = new StringBuilder(">>> Starting Seed");
        if (seedRuntime.getVersion() != null) {
            startMessage.append(" v").append(seedRuntime.getVersion());
        }
        LOGGER.info(startMessage.toString());

        // Banner
        String banner = getBanner();
        if (banner != null) {
            System.out.println(banner);
        }

        // Kernel
        if (kernelConfiguration == null) {
            kernelConfiguration = NuunCore.newKernelConfiguration();
        }
        kernelConfiguration.containerContext(seedRuntime);
        Kernel kernel = createKernel(kernelConfiguration, seedRuntime.getDiagnosticManager());
        if (autoStart) {
            kernel.start();
            LOGGER.info("Seed started");
        }

        return kernel;
    }

    public void disposeKernel(Kernel kernel) {
        if (kernel != null && kernel.isStarted()) {
            LOGGER.info("Stopping Seed");
            kernel.stop();
            LOGGER.info("<<< Seed stopped");
        }
    }

    private synchronized Kernel createKernel(KernelConfiguration kernelConfiguration, DiagnosticManager diagnosticManager) {
        // Kernel instantiation
        Kernel kernel = NuunCore.createKernel(kernelConfiguration);
        FallbackUrlType fallbackUrlType = new FallbackUrlType();
        List<Vfs.UrlType> urlTypes = new ArrayList<>(detectedUrlTypes);
        urlTypes.add(fallbackUrlType);

        LOGGER.debug("Registered URL types for classpath scan: " + urlTypes);

        // Kernel initialization (it is assumed that only this class alter Vfs default url types)
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

        diagnosticManager.registerDiagnosticInfoCollector("kernel", () -> {
            Map<String, Object> result = new HashMap<>();
            result.put("scannedUrls", kernel.scannedURLs());
            result.put("failedUrls", fallbackUrlType.getFailedUrls());
            return result;
        });

        return kernel;
    }

    private String getBanner() {
        InputStream bannerStream = ClassLoaders.findMostCompleteClassLoader(Seed.class).getResourceAsStream("banner.txt");
        if (bannerStream != null) {
            try {
                return new Scanner(bannerStream).useDelimiter("\\Z").next();
            } finally {
                try {
                    bannerStream.close();
                } catch (IOException e) {
                    // nothing to do
                }
            }
        }
        return null;
    }
}
