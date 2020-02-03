/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.command;

import static org.seedstack.shed.reflect.ReflectUtils.makeAccessible;

import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.api.plugin.request.ClasspathScanRequest;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.seedstack.seed.command.Argument;
import org.seedstack.seed.command.Command;
import org.seedstack.seed.command.Option;
import org.seedstack.seed.core.internal.AbstractSeedPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Nuun plugin in charge of collecting all commands definitions.
 */
public class CommandPlugin extends AbstractSeedPlugin {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandPlugin.class);

    private final Map<String, CommandDefinition> commandDefinitions = new HashMap<>();

    @Override
    public String name() {
        return "command";
    }

    @Override
    public InitState initialize(InitContext initContext) {
        Map<Class<? extends Annotation>, Collection<Class<?>>> scannedClassesByAnnotationClass = initContext
                .scannedClassesByAnnotationClass();

        Collection<Class<?>> commandAnnotatedClasses = scannedClassesByAnnotationClass.get(
                org.seedstack.seed.command.CommandDefinition.class);
        for (Class<?> candidate : commandAnnotatedClasses) {
            if (Command.class.isAssignableFrom(candidate)) {
                org.seedstack.seed.command.CommandDefinition commandDefinitionAnnotation = candidate.getAnnotation(
                        org.seedstack.seed.command.CommandDefinition.class);
                if (commandDefinitionAnnotation != null) {
                    CommandDefinition commandDefinition = new CommandDefinition(commandDefinitionAnnotation,
                            candidate.asSubclass(Command.class));

                    for (Field field : candidate.getDeclaredFields()) {
                        Argument argumentAnnotation = field.getAnnotation(Argument.class);
                        Option optionAnnotation = field.getAnnotation(Option.class);

                        makeAccessible(field);

                        if (argumentAnnotation != null) {
                            commandDefinition.addArgumentField(argumentAnnotation, field);
                        } else if (optionAnnotation != null) {
                            commandDefinition.addOptionField(optionAnnotation, field);
                        }
                    }

                    commandDefinitions.put(commandDefinition.getQualifiedName(), commandDefinition);
                    LOGGER.debug("Command {} detected, implemented in {}", commandDefinition.getQualifiedName(),
                            commandDefinition.getCommandActionClass().getCanonicalName());
                }
            }
        }

        return InitState.INITIALIZED;
    }

    @Override
    public Collection<ClasspathScanRequest> classpathScanRequests() {
        return classpathScanRequestBuilder().annotationType(org.seedstack.seed.command.CommandDefinition.class).build();
    }

    @Override
    public Object nativeUnitModule() {
        return new CommandModule(commandDefinitions);
    }
}
