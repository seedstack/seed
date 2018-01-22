/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.undertow.internal;

import com.google.common.collect.Lists;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.InitContext;
import java.util.Collection;
import javax.servlet.ServletContext;
import org.seedstack.seed.core.SeedRuntime;
import org.seedstack.seed.core.internal.AbstractSeedPlugin;
import org.seedstack.seed.crypto.spi.SSLProvider;
import org.seedstack.seed.spi.ConfigurationPriority;
import org.seedstack.seed.web.WebConfig;

/**
 * This plugin retrieves the Undertow Web server configuration.
 */
public class UndertowPlugin extends AbstractSeedPlugin {
    static final String NAME = "undertow";
    private SSLProvider sslProvider;

    @Override
    public String name() {
        return NAME;
    }

    @Override
    protected void setup(SeedRuntime seedRuntime) {
        ServletContext servletContext = seedRuntime.contextAs(ServletContext.class);
        if (servletContext != null) {
            seedRuntime.registerConfigurationProvider(
                    new UndertowRuntimeConfigurationProvider(
                            servletContext,
                            seedRuntime.getConfiguration().get(WebConfig.ServerConfig.class)
                    ),
                    ConfigurationPriority.RUNTIME_INFO
            );
        }
    }

    @Override
    public Collection<Class<?>> dependencies() {
        return Lists.newArrayList(SSLProvider.class);
    }

    @Override
    public InitState initialize(InitContext initContext) {
        sslProvider = initContext.dependency(SSLProvider.class);
        return InitState.INITIALIZED;
    }

    SSLProvider getSslProvider() {
        return sslProvider;
    }
}
