/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.init;

import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import org.hibernate.validator.parameternameprovider.ReflectionParameterNameProvider;
import org.seedstack.seed.core.internal.validation.SeedMessageInterpolator;

public class GlobalValidatorFactory {
    private final ValidatorFactory validatorFactory;

    private GlobalValidatorFactory() {
        validatorFactory = Validation.byDefaultProvider()
                .configure()
                .parameterNameProvider(new ReflectionParameterNameProvider())
                .messageInterpolator(new SeedMessageInterpolator())
                .buildValidatorFactory();
    }

    public static ValidatorFactory get() {
        return Holder.INSTANCE.validatorFactory;
    }

    private static class Holder {
        private static final GlobalValidatorFactory INSTANCE = new GlobalValidatorFactory();
    }
}
