/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.internal.command;

import java.lang.reflect.Field;
import org.seedstack.seed.command.Option;

/**
 * Holds the definition of a command option.
 */
class OptionDefinition {
    private final Option option;
    private final Field field;

    OptionDefinition(Option option, Field field) {
        this.option = option;
        this.field = field;
    }

    String getName() {
        return option.name();
    }

    String getLongName() {
        return option.longName();
    }

    boolean hasArgument() {
        return option.hasArgument();
    }

    String getDescription() {
        return option.description();
    }

    boolean isMandatory() {
        return option.mandatory();
    }

    String getDefaultValue() {
        return option.defaultValue();
    }

    Option getAnnotation() {
        return option;
    }

    Field getField() {
        return field;
    }
}
