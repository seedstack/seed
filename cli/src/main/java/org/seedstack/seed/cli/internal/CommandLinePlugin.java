/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.cli.internal;

import io.nuun.kernel.api.Plugin;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.api.plugin.request.ClasspathScanRequest;
import io.nuun.kernel.core.AbstractPlugin;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.configuration.Configuration;
import org.kametic.specifications.Specification;
import org.seedstack.seed.cli.api.CliCommand;
import org.seedstack.seed.cli.api.CommandLineHandler;
import org.seedstack.seed.cli.spi.CliContext;
import org.seedstack.seed.core.internal.application.ApplicationPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.seedstack.seed.core.utils.BaseClassSpecifications.ancestorImplements;
import static org.seedstack.seed.core.utils.BaseClassSpecifications.classIsAbstract;
import static org.seedstack.seed.core.utils.BaseClassSpecifications.classIsInterface;

/**
 * This plugin enables to run {@link org.seedstack.seed.cli.api.CommandLineHandler} through
 * {@link org.seedstack.seed.cli.SeedRunner}.
 *
 * @author epo.jemba@ext.mpsa.com
 * @author adrien.lauer@mpsa.com
 */
public class CommandLinePlugin extends AbstractPlugin {
    public static final String CLI_PLUGIN_PREFIX = "org.seedstack.seed.cli";
    public static final String DEFAULT_COMMAND_CONFIG_KEY = CLI_PLUGIN_PREFIX + ".default-command";
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandLinePlugin.class);

    private final Specification<Class<?>> cliHandlerSpec = and(ancestorImplements(CommandLineHandler.class), not(classIsInterface()), not(classIsAbstract()));
    private final Map<String, Class<? extends CommandLineHandler>> cliHandlers = new HashMap<String, Class<? extends CommandLineHandler>>();

    private CliContext cliContext;

    @Override
    public String name() {
        return "seed-cli-plugin";
    }

    @Override
    public Collection<ClasspathScanRequest> classpathScanRequests() {
        return classpathScanRequestBuilder().specification(cliHandlerSpec).build();
    }

    @Override
    public Collection<Class<? extends Plugin>> requiredPlugins() {
        Collection<Class<? extends Plugin>> plugins = new ArrayList<Class<? extends Plugin>>();
        plugins.add(ApplicationPlugin.class);
        return plugins;
    }

    @Override
    public void provideContainerContext(Object containerContext) {
        if (containerContext instanceof CliContext) {
            cliContext = (CliContext) containerContext;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public InitState init(InitContext initContext) {
        if (cliContext == null) {
            LOGGER.info("No command line environment detected, CLI support disabled");
            return InitState.INITIALIZED;
        }

        Configuration cliConfiguration = ((ApplicationPlugin) initContext.pluginsRequired().iterator().next()).getApplication().getConfiguration().subset(CLI_PLUGIN_PREFIX);
        Collection<Class<?>> cliHandlerCandidates = initContext.scannedTypesBySpecification().get(cliHandlerSpec);

        for (Class<?> candidate : cliHandlerCandidates) {
            CliCommand cliCommand = candidate.getAnnotation(CliCommand.class);
            if (CommandLineHandler.class.isAssignableFrom(candidate) && cliCommand != null) {
                LOGGER.trace("Detected CLI handler {}", candidate.getCanonicalName());
                cliHandlers.put(cliCommand.value(), (Class<? extends CommandLineHandler>) candidate);
            }
        }

        LOGGER.debug("Detected {} CLI handler(s)", cliHandlers.size());

        String defaultCommand = cliConfiguration.getString(DEFAULT_COMMAND_CONFIG_KEY);
        String[] effectiveArgs;
        if (defaultCommand == null) {
        } else {
            effectiveArgs = cliContext.getArgs();
        }

        PosixParser posixParser = new PosixParser();


        return InitState.INITIALIZED;
    }

    @Override
    public Object nativeUnitModule() {
        return new CommandLineModule(cliHandlers);
    }
}
