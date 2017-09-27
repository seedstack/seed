/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.internal.cli;

import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.cli.CliArgs;
import org.seedstack.seed.cli.CliCommand;
import org.seedstack.seed.cli.CliContext;
import org.seedstack.seed.cli.CliOption;

/**
 * Guice type listener that will register any type having a field annotated with
 * {@link org.seedstack.seed.cli.CliArgs} or
 * {@link org.seedstack.seed.cli.CliOption}.
 */
class CliTypeListener implements TypeListener {
    private final CliContext cliContext;

    CliTypeListener(CliContext cliContext) {
        this.cliContext = cliContext;
    }

    @Override
    public <T> void hear(TypeLiteral<T> typeLiteral, TypeEncounter<T> typeEncounter) {
        Set<Field> fields = new HashSet<>();
        CliCommand cliCommand = null;
        for (Class<?> c = typeLiteral.getRawType(); c != Object.class; c = c.getSuperclass()) {
            if (cliCommand == null) {
                cliCommand = c.getAnnotation(CliCommand.class);
            } else {
                throw SeedException.createNew(CliErrorCode.CONFLICTING_COMMAND_ANNOTATIONS).put("class",
                        c.getCanonicalName());
            }
            Arrays.stream(c.getDeclaredFields()).filter(this::isCandidate).forEach(fields::add);
        }

        if (!fields.isEmpty()) {
            typeEncounter.register(new CliMembersInjector<>(
                    cliContext,
                    cliCommand == null ? typeLiteral.getType().getTypeName() : cliCommand.value(),
                    fields)
            );
        }
    }

    private boolean isCandidate(Field field) {
        return field.isAnnotationPresent(CliArgs.class) || field.isAnnotationPresent(CliOption.class);
    }
}
