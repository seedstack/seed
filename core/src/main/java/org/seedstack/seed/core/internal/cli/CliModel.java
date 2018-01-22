/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.internal.cli;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.seedstack.seed.cli.CliArgs;
import org.seedstack.seed.cli.CliOption;

class CliModel {
    private final Options options = new Options();
    private final List<CliOption> optionAnnotations = new ArrayList<>();
    private final List<Field> optionFields = new ArrayList<>();
    private Field argsField;
    private int mandatoryArgsCount;

    CliModel(Set<Field> fields) {
        for (Field field : fields) {
            CliOption optionAnnotation = field.getAnnotation(CliOption.class);
            CliArgs argsAnnotation = field.getAnnotation(CliArgs.class);

            if (optionAnnotation != null) {
                Option option = new Option(
                        optionAnnotation.name(),
                        optionAnnotation.longName(),
                        optionAnnotation.valueCount() > 0 || optionAnnotation.valueCount() == -1,
                        optionAnnotation.description()
                );

                if (optionAnnotation.valueCount() == -1) {
                    option.setArgs(Option.UNLIMITED_VALUES);
                } else if (optionAnnotation.valueCount() > 0) {
                    option.setArgs(optionAnnotation.valueCount());
                }

                option.setValueSeparator(optionAnnotation.valueSeparator());
                option.setRequired(optionAnnotation.mandatory());
                option.setOptionalArg(!optionAnnotation.mandatoryValue());
                optionAnnotations.add(optionAnnotation);
                optionFields.add(field);
                options.addOption(option);
            } else if (argsAnnotation != null) {
                mandatoryArgsCount = argsAnnotation.mandatoryCount();
                argsField = field;
            }
        }
    }

    Options getOptions() {
        return options;
    }

    List<CliOption> getOptionAnnotations() {
        return optionAnnotations;
    }

    List<Field> getOptionFields() {
        return optionFields;
    }

    Field getArgsField() {
        return argsField;
    }

    int getMandatoryArgsCount() {
        return mandatoryArgsCount;
    }
}
