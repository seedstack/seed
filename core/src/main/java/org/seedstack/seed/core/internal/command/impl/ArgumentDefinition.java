/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.command.impl;

import com.google.common.base.Strings;
import org.seedstack.seed.spi.command.Argument;

import java.lang.reflect.Field;

/**
 * Holds the definition of a command argument.
 *
 * @author adrien.lauer@mpsa.com
 */
class ArgumentDefinition implements Comparable<ArgumentDefinition> {
    private final Field field;
    private final Argument argument;

    ArgumentDefinition(Argument argument, Field field) {
        this.field = field;
        this.argument = argument;
    }

    Field getField() {
        return field;
    }

    String getName() {
        return Strings.isNullOrEmpty(argument.name()) ? "arg" + argument.index() : argument.name();
    }

    String getDescription() {
        return argument.description();
    }

    boolean isMandatory() {
        return argument.mandatory();
    }

    String getDefaultValue() {
        return argument.defaultValue();
    }

    Argument getAnnotation() {
        return argument;
    }

    @Override
    public int compareTo(ArgumentDefinition o) {
        return Integer.valueOf(argument.index()).compareTo(o.argument.index());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        return argument.index() == ((ArgumentDefinition) o).argument.index();
    }

    @Override
    public int hashCode() {
        return Integer.valueOf(argument.index()).hashCode();
    }
}
