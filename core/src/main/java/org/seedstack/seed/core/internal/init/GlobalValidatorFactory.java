/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.init;

import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;

import javax.validation.Validation;
import javax.validation.ValidatorFactory;

public class GlobalValidatorFactory {
    private final ValidatorFactory validatorFactory;

    private static class Holder {
        private static final GlobalValidatorFactory INSTANCE = new GlobalValidatorFactory();
    }

    public static ValidatorFactory get() {
        return Holder.INSTANCE.validatorFactory;
    }

    private GlobalValidatorFactory() {
        validatorFactory = Validation.byDefaultProvider()
                .configure()
                .messageInterpolator(new ParameterMessageInterpolator())
                .buildValidatorFactory();
    }
}
