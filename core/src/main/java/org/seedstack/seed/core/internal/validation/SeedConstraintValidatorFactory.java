/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.internal.validation;

import com.google.inject.Injector;
import com.google.inject.ProvisionException;
import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorFactory;
import org.seedstack.shed.reflect.Classes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class SeedConstraintValidatorFactory implements ConstraintValidatorFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(SeedConstraintValidatorFactory.class);
    private final Injector injector;

    @Inject
    SeedConstraintValidatorFactory(Injector injector) {
        this.injector = injector;
    }

    @Override
    public <T extends ConstraintValidator<?, ?>> T getInstance(Class<T> key) {
        if (key.getName().startsWith("org.hibernate.validator")) {
            // Hibernate constraint validators are instantiated directly (no injection possible nor needed)
            return Classes.instantiateDefault(key);
        } else {
            try {
                return injector.getInstance(key);
            } catch (ProvisionException e) {
                LOGGER.warn("Constraint validator {} was not detected by SeedStack and is not injectable",
                        key.getName());
                return Classes.instantiateDefault(key);
            }
        }
    }

    @Override
    public void releaseInstance(ConstraintValidator<?, ?> instance) {
        // nothing to do
    }
}
