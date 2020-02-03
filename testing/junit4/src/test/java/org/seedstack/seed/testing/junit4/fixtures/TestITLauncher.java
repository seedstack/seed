/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.testing.junit4.fixtures;

import com.google.inject.AbstractModule;
import io.nuun.kernel.api.Kernel;
import io.nuun.kernel.api.config.KernelConfiguration;
import io.nuun.kernel.api.plugin.PluginException;
import io.nuun.kernel.core.AbstractPlugin;
import io.nuun.kernel.core.NuunCore;
import java.util.Map;
import java.util.Optional;
import org.seedstack.seed.Application;
import org.seedstack.seed.spi.SeedLauncher;

/**
 * This {@link SeedLauncher} is the default launcher for integration tests.
 */
public class TestITLauncher implements SeedLauncher {
    private Kernel kernel;

    private static int launchCount = 0;

    @Override
    public void launch(String[] args, Map<String, String> kernelParameters) {
        launchCount += 1;
        KernelConfiguration kernelConfiguration = NuunCore.newKernelConfiguration();
        for (Map.Entry<String, String> kernelParameter : kernelParameters.entrySet()) {
            kernelConfiguration.param(kernelParameter.getKey(), kernelParameter.getValue());
        }
        kernelConfiguration.withoutSpiPluginsLoader();
        kernelConfiguration.rootPackages("org.seedstack");
        kernelConfiguration.addPlugin(new AbstractPlugin() {
            @Override
            public String name() {
                return "test";
            }

            @Override
            public Object nativeUnitModule() {
                return new AbstractModule() {
                    @Override
                    protected void configure() {
                        try {
                            bind(Class.forName(kernelParameters.get("seedstack.it.testClassName")));
                            bind(Application.class).toInstance(new ApplicationImpl(args, kernelParameters));
                        } catch (ClassNotFoundException e) {
                            throw new PluginException(e);
                        }
                    }
                };
            }
        });

        try {
            kernel = NuunCore.createKernel(kernelConfiguration);
            kernel.init();
            kernel.start();
        } catch (Exception e) {
            kernel = null;
            throw e;
        }
    }

    @Override
    public Optional<Kernel> getKernel() {
        return Optional.ofNullable(kernel);
    }

    public static void resetLaunchCount() {
        launchCount = 0;
    }

    public static int getLaunchCount() {
        return launchCount;
    }

    @Override
    public void shutdown() {
        try {
            if (kernel != null && kernel.isStarted()) {
                kernel.stop();
            }
        } finally {
            kernel = null;
        }
    }
}
