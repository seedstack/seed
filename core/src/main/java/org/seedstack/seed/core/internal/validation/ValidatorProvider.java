/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.validation;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

class ValidatorProvider implements Provider<Validator> {
    private final ValidatorFactory validatorFactory;

    @Inject
    ValidatorProvider(ValidatorFactory validatorFactory) {
        this.validatorFactory = validatorFactory;
    }

    @Override
    public Validator get() {
        return validatorFactory.getValidator();
    }
}
