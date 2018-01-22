/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.internal.validation;

import javax.validation.ValidatorFactory;
import org.seedstack.seed.core.SeedRuntime;
import org.seedstack.seed.core.internal.AbstractSeedPlugin;

/**
 * This plugin handles validation through jsr303 and jsr349.
 */
public class ValidationPlugin extends AbstractSeedPlugin {
    private ValidatorFactory validatorFactory = null;

    @Override
    public String name() {
        return "validation";
    }

    @Override
    protected void setup(SeedRuntime seedRuntime) {
        validatorFactory = seedRuntime.getValidatorFactory();
    }

    @Override
    public Object nativeUnitModule() {
        if (validatorFactory != null) {
            return new ValidationModule(validatorFactory);
        } else {
            return null;
        }
    }
}
