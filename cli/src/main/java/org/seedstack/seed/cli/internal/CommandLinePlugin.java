/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.cli.internal;

import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.api.plugin.request.ClasspathScanRequest;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.seedstack.seed.cli.CliCommand;
import org.seedstack.seed.cli.CliContext;
import org.seedstack.seed.cli.CommandLineHandler;
import org.seedstack.seed.core.SeedRuntime;
import org.seedstack.seed.core.internal.AbstractSeedPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This plugin enables to run {@link CommandLineHandler} through
 * {@link CliLauncher}.
 */
public class CommandLinePlugin extends AbstractSeedPlugin {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandLinePlugin.class);
    private final Map<String, Class<? extends CommandLineHandler>> cliHandlers = new HashMap<>();

    private CliContext cliContext;

    @Override
    public String name() {
        return "command-line";
    }

    @Override
    public void setup(SeedRuntime seedRuntime) {
        cliContext = seedRuntime.contextAs(CliContext.class);
    }

    @Override
    public Collection<ClasspathScanRequest> classpathScanRequests() {
        return classpathScanRequestBuilder().specification(CommandLineHandlerSpecification.INSTANCE).build();
    }

    @Override
    @SuppressWarnings("unchecked")
    public InitState initialize(InitContext initContext) {
        if (cliContext == null) {
            LOGGER.info("No command line environment detected, CLI support disabled");
            return InitState.INITIALIZED;
        }

        Collection<Class<?>> cliHandlerCandidates = initContext.scannedTypesBySpecification()
                .get(CommandLineHandlerSpecification.INSTANCE);
        for (Class<?> candidate : cliHandlerCandidates) {
            CliCommand cliCommand = candidate.getAnnotation(CliCommand.class);
            if (CommandLineHandler.class.isAssignableFrom(candidate) && cliCommand != null) {
                LOGGER.debug("Detected CLI handler {}", candidate.getCanonicalName());
                cliHandlers.put(cliCommand.value(), (Class<? extends CommandLineHandler>) candidate);
            }
        }
        LOGGER.info("Detected {} CLI handler(s)", cliHandlers.size());

        return InitState.INITIALIZED;
    }

    @Override
    public Object nativeUnitModule() {
        return new CommandLineModule(cliHandlers);
    }
}
