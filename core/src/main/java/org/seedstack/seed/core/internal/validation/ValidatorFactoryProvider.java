/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.internal.validation;

import com.google.inject.Injector;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.validation.ValidatorFactory;
import org.seedstack.seed.core.internal.init.ValidationManager;

class ValidatorFactoryProvider implements Provider<ValidatorFactory> {
    private final Injector injector;

    @Inject
    ValidatorFactoryProvider(Injector injector) {
        this.injector = injector;
    }

    @Override
    public ValidatorFactory get() {
        return ValidationManager.get().createValidatorFactory(c ->
                c.constraintValidatorFactory(new SeedConstraintValidatorFactory(injector))
        );
    }
}
