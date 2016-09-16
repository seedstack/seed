/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.cli.internal;

import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.api.plugin.request.ClasspathScanRequest;
import io.nuun.kernel.core.AbstractPlugin;
import org.kametic.specifications.Specification;
import org.seedstack.seed.core.SeedRuntime;
import org.seedstack.seed.cli.CliCommand;
import org.seedstack.seed.cli.CommandLineHandler;
import org.seedstack.seed.cli.spi.CliContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.seedstack.seed.core.utils.BaseClassSpecifications.ancestorImplements;
import static org.seedstack.seed.core.utils.BaseClassSpecifications.classIsAbstract;
import static org.seedstack.seed.core.utils.BaseClassSpecifications.classIsInterface;

/**
 * This plugin enables to run {@link CommandLineHandler} through
 * {@link org.seedstack.seed.cli.SeedRunner}.
 *
 * @author epo.jemba@ext.mpsa.com
 * @author adrien.lauer@mpsa.com
 */
public class CommandLinePlugin extends AbstractPlugin {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandLinePlugin.class);

    private final Specification<Class<?>> cliHandlerSpec = and(ancestorImplements(CommandLineHandler.class), not(classIsInterface()), not(classIsAbstract()));
    private final Map<String, Class<? extends CommandLineHandler>> cliHandlers = new HashMap<>();

    private CliContext cliContext;

    @Override
    public String name() {
        return "cli";
    }

    @Override
    public void provideContainerContext(Object containerContext) {
        cliContext = ((SeedRuntime) containerContext).contextAs(CliContext.class);
    }

    @Override
    public Collection<ClasspathScanRequest> classpathScanRequests() {
        return classpathScanRequestBuilder().specification(cliHandlerSpec).build();
    }

    @Override
    @SuppressWarnings("unchecked")
    public InitState init(InitContext initContext) {
        if (cliContext == null) {
            LOGGER.info("No command line environment detected, CLI support disabled");
            return InitState.INITIALIZED;
        }

        Collection<Class<?>> cliHandlerCandidates = initContext.scannedTypesBySpecification().get(cliHandlerSpec);
        for (Class<?> candidate : cliHandlerCandidates) {
            CliCommand cliCommand = candidate.getAnnotation(CliCommand.class);
            if (CommandLineHandler.class.isAssignableFrom(candidate) && cliCommand != null) {
                LOGGER.trace("Detected CLI handler {}", candidate.getCanonicalName());
                cliHandlers.put(cliCommand.value(), (Class<? extends CommandLineHandler>) candidate);
            }
        }
        LOGGER.debug("Detected {} CLI handler(s)", cliHandlers.size());

        return InitState.INITIALIZED;
    }

    @Override
    public Object nativeUnitModule() {
        return new CommandLineModule(cliHandlers);
    }
}
