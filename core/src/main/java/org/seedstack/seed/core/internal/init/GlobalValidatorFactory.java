/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.internal.init;

import javax.validation.Configuration;
import javax.validation.ParameterNameProvider;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import org.seedstack.seed.core.internal.validation.SeedMessageInterpolator;
import org.seedstack.shed.reflect.Classes;

public class GlobalValidatorFactory {
    private final ValidatorFactory validatorFactory;

    private GlobalValidatorFactory() {
        Configuration<?> configuration = Validation.byDefaultProvider().configure();
        configureParameterNameProvider(configuration);
        validatorFactory = configuration
                .messageInterpolator(new SeedMessageInterpolator())
                .buildValidatorFactory();
    }

    @SuppressWarnings("deprecation")
    private void configureParameterNameProvider(Configuration<?> configuration) {
        try {
            Configuration.class.getMethod("parameterNameProvider", ParameterNameProvider.class);
            Classes.<org.hibernate.validator.parameternameprovider.ReflectionParameterNameProvider>optional(
                    "org.hibernate.validator.parameternameprovider.ReflectionParameterNameProvider")
                    .map(Classes::instantiateDefault)
                    .ifPresent(configuration::parameterNameProvider);
        } catch (NoSuchMethodException e) {
            // ignore
        }
    }

    public static ValidatorFactory get() {
        return Holder.INSTANCE.validatorFactory;
    }

    private static class Holder {
        private static final GlobalValidatorFactory INSTANCE = new GlobalValidatorFactory();
    }
}
