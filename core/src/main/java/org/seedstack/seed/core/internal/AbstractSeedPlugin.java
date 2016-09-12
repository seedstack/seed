/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal;

import com.google.common.collect.Lists;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.core.AbstractPlugin;
import org.seedstack.coffig.Coffig;
import org.seedstack.seed.Application;
import org.seedstack.seed.DiagnosticManager;
import org.seedstack.seed.SeedRuntime;
import org.seedstack.seed.core.Seed;
import org.seedstack.seed.spi.config.ApplicationProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class AbstractSeedPlugin extends AbstractPlugin {
    private Application application;
    private SeedRuntime seedRuntime;

    @Override
    public final void provideContainerContext(Object containerContext) {
        seedRuntime = ((SeedRuntime) containerContext);
        setup();
    }

    @Override
    public final Collection<Class<?>> requiredPlugins() {
        List<Class<?>> dependencies = new ArrayList<>(dependencies());
        dependencies.add(ApplicationProvider.class);
        return dependencies;
    }

    @Override
    public final InitState init(InitContext initContext) {
        application = initContext.dependency(ApplicationProvider.class).getApplication();
        return initialize(initContext);
    }

    protected void setup() {
    }

    protected InitState initialize(InitContext initContext) {
        return InitState.INITIALIZED;
    }

    protected Collection<Class<?>> dependencies() {
        return Lists.newArrayList();
    }

    protected DiagnosticManager getDiagnosticManager() {
        return getSeedRuntime().getDiagnosticManager();
    }

    protected Coffig getBaseConfiguration() {
        return Seed.baseConfiguration();
    }

    protected SeedRuntime getSeedRuntime() {
        if (seedRuntime == null) {
            throw new IllegalStateException("Seed runtime is not yet available");
        }
        return seedRuntime;
    }

    protected Application getApplication() {
        if (application == null) {
            throw new IllegalStateException("Configuration is not available before plugin initialization");
        }
        return application;
    }

    protected Coffig getConfiguration() {
        return getApplication().getConfiguration();
    }

    protected <T> T getConfiguration(Class<T> configClass, String... path) {
        return getConfiguration().get(configClass, path);
    }
}
