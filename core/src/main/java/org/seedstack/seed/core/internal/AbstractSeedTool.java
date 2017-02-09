/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal;

import org.seedstack.seed.core.internal.cli.CliPlugin;
import org.seedstack.seed.core.internal.configuration.ConfigurationPlugin;
import org.seedstack.seed.spi.SeedTool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public abstract class AbstractSeedTool extends AbstractSeedPlugin implements SeedTool {
    @Override
    public final String name() {
        return toolName() + "-tool";
    }

    @Override
    public final Collection<Class<?>> pluginsToLoad() {
        List<Class<?>> dependencies = new ArrayList<>(toolPlugins());
        dependencies.add(CorePlugin.class);
        dependencies.add(ConfigurationPlugin.class);
        dependencies.add(CliPlugin.class);
        return dependencies;
    }

    protected Collection<Class<?>> toolPlugins() {
        return Collections.emptyList();
    }

    public abstract String toolName();
}
