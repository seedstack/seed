/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.cli.internal;

import java.lang.reflect.Modifier;
import java.util.function.Predicate;
import org.seedstack.seed.cli.CommandLineHandler;
import org.seedstack.shed.reflect.ClassPredicates;

class CommandLineHandlerSpecification implements Predicate<Class<?>> {
    static CommandLineHandlerSpecification INSTANCE = new CommandLineHandlerSpecification();

    private CommandLineHandlerSpecification() {
        // no instantiation allowed
    }

    @Override
    public boolean test(Class<?> candidate) {
        return ClassPredicates.classIsAssignableFrom(CommandLineHandler.class)
                .and(ClassPredicates.classIsInterface().negate())
                .and(ClassPredicates.classModifierIs(Modifier.ABSTRACT).negate())
                .test(candidate);
    }
}
