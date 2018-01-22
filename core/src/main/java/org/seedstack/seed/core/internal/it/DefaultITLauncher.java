/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.internal.it;

import io.nuun.kernel.api.Kernel;
import io.nuun.kernel.api.config.KernelConfiguration;
import io.nuun.kernel.core.NuunCore;
import java.util.Map;
import java.util.Optional;
import org.seedstack.seed.cli.CliContext;
import org.seedstack.seed.core.Seed;
import org.seedstack.seed.spi.SeedLauncher;

/**
 * This {@link SeedLauncher} is the default launcher for integration tests.
 */
public class DefaultITLauncher implements SeedLauncher {
    private Kernel kernel;

    @Override
    public void launch(String[] args, Map<String, String> kernelParameters) {
        KernelConfiguration kernelConfiguration = NuunCore.newKernelConfiguration();
        for (Map.Entry<String, String> kernelParameter : kernelParameters.entrySet()) {
            kernelConfiguration.param(kernelParameter.getKey(), kernelParameter.getValue());
        }

        try {
            kernel = Seed.createKernel((CliContext) () -> args, kernelConfiguration, true);
        } catch (Exception e) {
            kernel = null;
            throw e;
        }
    }

    @Override
    public Optional<Kernel> getKernel() {
        return Optional.ofNullable(kernel);
    }

    @Override
    public void shutdown() {
        try {
            Seed.disposeKernel(kernel);
        } finally {
            kernel = null;
        }
    }
}
