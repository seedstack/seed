/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.internal;

import com.google.common.collect.Lists;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.nuun.kernel.api.Kernel;
import io.nuun.kernel.api.Plugin;
import io.nuun.kernel.api.config.KernelConfiguration;
import io.nuun.kernel.core.NuunCore;
import java.util.ServiceLoader;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.core.Seed;
import org.seedstack.seed.spi.SeedLauncher;
import org.seedstack.seed.spi.SeedTool;

public class ToolLauncher implements SeedLauncher {
    private final String toolName;
    private Kernel kernel;

    public ToolLauncher(String toolName) {
        this.toolName = toolName;
    }

    @Override
    @SuppressFBWarnings(value = "DM_EXIT", justification = "ToolLauncher must be able to return a code to the system")
    public void launch(String[] args) throws Exception {
        // no logs wanted for tools
        Seed.disableLogs();

        SeedTool seedTool = null;
        for (org.seedstack.seed.spi.SeedTool candidate : Lists.newArrayList(
                ServiceLoader.load(org.seedstack.seed.spi.SeedTool.class))) {
            if (toolName.equals(candidate.toolName())) {
                if (seedTool == null) {
                    seedTool = candidate;
                } else {
                    throw SeedException.createNew(CoreErrorCode.MULTIPLE_TOOLS_WITH_IDENTICAL_NAMES).put("toolName",
                            toolName);
                }
            }
        }
        if (seedTool == null) {
            throw SeedException.createNew(CoreErrorCode.TOOL_NOT_FOUND).put("toolName", toolName);
        } else {
            kernel = Seed.createKernel(new ToolContext(toolName, args), buildKernelConfiguration(seedTool), true);
            System.exit(((SeedTool) kernel.plugins().get(seedTool.name())).call());
        }
    }

    @SuppressWarnings("unchecked")
    private KernelConfiguration buildKernelConfiguration(SeedTool seedTool) {
        KernelConfiguration kernelConfiguration = NuunCore.newKernelConfiguration();
        if (seedTool.startMode() == SeedTool.StartMode.MINIMAL) {
            kernelConfiguration
                    .param(CorePlugin.AUTODETECT_MODULES_KERNEL_PARAM, "false")
                    .param(CorePlugin.AUTODETECT_BINDINGS_KERNEL_PARAM, "false")
                    .withoutSpiPluginsLoader();
        }
        kernelConfiguration.addPlugin(seedTool.getClass());
        seedTool.pluginsToLoad().forEach(
                pluginClass -> kernelConfiguration.addPlugin((Class<? extends Plugin>) pluginClass));
        return kernelConfiguration;
    }

    @Override
    public void shutdown() throws Exception {
        Seed.disposeKernel(kernel);
    }
}
