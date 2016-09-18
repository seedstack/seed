/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal;

import com.google.common.collect.Lists;
import io.nuun.kernel.api.Kernel;
import io.nuun.kernel.api.Plugin;
import io.nuun.kernel.core.NuunCore;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.core.Seed;
import org.seedstack.seed.spi.SeedLauncher;
import org.seedstack.seed.spi.SeedTool;
import org.seedstack.seed.spi.ToolContext;

import java.util.ServiceLoader;

public class ToolLauncher implements SeedLauncher {
    private final String toolName;

    public ToolLauncher(String toolName) {
        this.toolName = toolName;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void launch(String[] args) throws Exception {
        Seed.disableLogs();

        SeedTool seedTool = null;
        for (org.seedstack.seed.spi.SeedTool candidate : Lists.newArrayList(ServiceLoader.load(org.seedstack.seed.spi.SeedTool.class))) {
            if (toolName.equals(candidate.toolName())) {
                if (seedTool == null) {
                    seedTool = candidate;
                } else {
                    throw SeedException.createNew(CoreErrorCode.MULTIPLE_TOOLS_WITH_IDENTICAL_NAMES).put("toolName", toolName);
                }
            }
        }
        if (seedTool == null) {
            throw SeedException.createNew(CoreErrorCode.TOOL_NOT_FOUND).put("toolName", toolName);
        } else if (seedTool instanceof AbstractSeedTool) {
            Kernel kernel = Seed.createKernel(new ToolContext(args), NuunCore.newKernelConfiguration().addPlugin((Class<? extends Plugin>) seedTool.getClass()), false);
            System.exit(((SeedTool) kernel.plugins().get(((AbstractSeedTool) seedTool).name())).call());
        } else {
            throw SeedException.createNew(CoreErrorCode.INVALID_TOOL).put("toolName", toolName).put("toolClass", seedTool.getClass());
        }
    }

    @Override
    public void shutdown() throws Exception {
        // nothing to do here
    }
}
