/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.command.impl;

import org.seedstack.seed.spi.command.Argument;
import org.seedstack.seed.spi.command.Command;
import org.seedstack.seed.spi.command.Option;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.api.plugin.request.ClasspathScanRequest;
import io.nuun.kernel.core.AbstractPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Nuun plugin in charge of collecting all commands definitions.
 *
 * @author adrien.lauer@mpsa.com
 */
public class CommandPlugin extends AbstractPlugin {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandPlugin.class);

    private final Map<String, CommandDefinition> commandDefinitions = new HashMap<String, CommandDefinition>();

    @Override
    public String name() {
        return "command";
    }

    @Override
    public InitState init(InitContext initContext) {
        Map<Class<? extends Annotation>, Collection<Class<?>>> scannedClassesByAnnotationClass = initContext.scannedClassesByAnnotationClass();

        Collection<Class<?>> commandAnnotatedClasses = scannedClassesByAnnotationClass.get(org.seedstack.seed.spi.command.CommandDefinition.class);
        for (Class<?> candidate : commandAnnotatedClasses) {
            if (Command.class.isAssignableFrom(candidate)) {
                org.seedstack.seed.spi.command.CommandDefinition commandDefinitionAnnotation = candidate.getAnnotation(org.seedstack.seed.spi.command.CommandDefinition.class);
                if (commandDefinitionAnnotation != null) {
                    CommandDefinition commandDefinition = new CommandDefinition(commandDefinitionAnnotation, candidate.asSubclass(Command.class));

                    for (Field field : candidate.getDeclaredFields()) {
                        Argument argumentAnnotation = field.getAnnotation(Argument.class);
                        Option optionAnnotation = field.getAnnotation(Option.class);

                        field.setAccessible(true);

                        if (argumentAnnotation != null) {
                            commandDefinition.addArgumentField(argumentAnnotation, field);
                        } else if (optionAnnotation != null) {
                            commandDefinition.addOptionField(optionAnnotation, field);
                        }
                    }

                    commandDefinitions.put(commandDefinition.getQualifiedName(), commandDefinition);
                    LOGGER.trace("Command {} registered with {}", commandDefinition.getQualifiedName(), commandDefinition.getCommandActionClass().getCanonicalName());
                }
            }
        }
        LOGGER.debug("Registered " + commandDefinitions.size() + " command(s)");

        return InitState.INITIALIZED;
    }

    @Override
    public Collection<ClasspathScanRequest> classpathScanRequests() {
        return classpathScanRequestBuilder().annotationType(org.seedstack.seed.spi.command.CommandDefinition.class).build();
    }

    @Override
    public Object nativeUnitModule() {
        return new CommandModule(commandDefinitions);
    }
}
