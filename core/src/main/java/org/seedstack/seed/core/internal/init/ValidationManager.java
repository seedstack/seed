/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.init;

import static org.seedstack.shed.ClassLoaders.findMostCompleteClassLoader;

import java.util.function.Consumer;
import javax.validation.Configuration;
import javax.validation.MessageInterpolator;
import javax.validation.ParameterNameProvider;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.core.internal.CoreErrorCode;
import org.seedstack.shed.reflect.Classes;

public class ValidationManager {
    private static final String VALIDATION_XML_FILE = "META-INF/validation.xml";
    private static final String SEEDSTACK_VALIDATION_AUTOCONFIG = "seedstack.validation.autoconfig";
    private static final String SEEDSTACK_VALIDATION_DISABLE = "seedstack.validation.disable";
    private final ValidationLevel validationLevel = determineValidationLevel();

    private ValidationManager() {
        // no external instantiation
    }

    public ValidationLevel getValidationLevel() {
        return validationLevel;
    }

    public ValidatorFactory createValidatorFactory(Consumer<Configuration> customizer) {
        boolean skipAutoconfig = "false".equalsIgnoreCase(System.getProperty(SEEDSTACK_VALIDATION_AUTOCONFIG, "true"));
        boolean hasXmlConfiguration = findMostCompleteClassLoader(ValidationManager.class)
                .getResource(VALIDATION_XML_FILE) != null;
        if (validationLevel == ValidationLevel.NONE) {
            throw SeedException.createNew(CoreErrorCode.UNABLE_TO_CREATE_VALIDATOR_FACTORY);
        } else {
            try {
                Configuration<?> configuration = Validation.byDefaultProvider().configure();
                if (!hasXmlConfiguration && !skipAutoconfig) {
                    Classes.optional("org.seedstack.seed.core.internal.validation.ReflectionParameterNameProvider")
                            .map(Classes::instantiateDefault)
                            .ifPresent(c -> configuration.parameterNameProvider((ParameterNameProvider) c));
                    Classes.optional("org.seedstack.seed.core.internal.validation.HibernateMessageInterpolator")
                            .map(Classes::instantiateDefault)
                            .ifPresent(c -> configuration.messageInterpolator((MessageInterpolator) c));
                }
                if (customizer != null) {
                    customizer.accept(configuration);
                }
                return configuration.buildValidatorFactory();
            } catch (Exception e) {
                throw SeedException.wrap(e, CoreErrorCode.UNABLE_TO_CREATE_VALIDATOR_FACTORY);
            }
        }
    }

    private ValidationLevel determineValidationLevel() {
        if (Classes.optional("javax.validation.Validation").isPresent()
                && !"true".equalsIgnoreCase(System.getProperty(SEEDSTACK_VALIDATION_DISABLE, "false"))) {
            try {
                Validation.byDefaultProvider().configure();
            } catch (Exception e) {
                return ValidationLevel.NONE;
            }
            if (Classes.optional("javax.validation.ClockProvider").isPresent()) {
                return ValidationLevel.LEVEL_2_0;
            } else if (Classes.optional("javax.validation.executable.ExecutableValidator").isPresent()) {
                return ValidationLevel.LEVEL_1_1;
            } else {
                return ValidationLevel.LEVEL_1_0;
            }
        }
        return ValidationLevel.NONE;
    }

    public enum ValidationLevel {
        NONE(""),
        LEVEL_1_0("1.0"),
        LEVEL_1_1("1.1"),
        LEVEL_2_0("2.0");

        private final String version;

        ValidationLevel(String version) {
            this.version = version;
        }

        @Override
        public String toString() {
            return version;
        }
    }

    private static class Holder {
        private static final ValidationManager INSTANCE = new ValidationManager();
    }

    public static ValidationManager get() {
        return Holder.INSTANCE;
    }
}
