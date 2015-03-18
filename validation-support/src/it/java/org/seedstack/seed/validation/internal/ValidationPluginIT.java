/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.validation.internal;

import org.seedstack.seed.validation.api.ValidationException;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.seedstack.seed.it.SeedITRunner;
import org.seedstack.seed.validation.internal.pojo.*;

import javax.inject.Inject;

@RunWith(SeedITRunner.class)
public class ValidationPluginIT {

    @Inject
    DummyServiceParamValidation serviceParam;

    @Inject
    DummyServiceFieldValidationOK serviceField;

    @Inject
    DummyServiceParamReturnType serviceReturnType;

    @Inject
    DummyServiceWithoutValidation serviceWithoutValidation;

    @Test
    public void services_are_well_injected() {
        Assertions.assertThat(serviceParam).isNotNull();
        Assertions.assertThat(serviceField).isNotNull();
        Assertions.assertThat(serviceReturnType).isNotNull();
        Assertions.assertThat(serviceWithoutValidation).isNotNull();
    }

    @Test
    public void param_not_null_validations_ok() {
        serviceParam.validateNotNullParam("");
    }

    @Test(expected = ValidationException.class)
    public void param_not_null_validations_are_well_intercepted() {
        serviceParam.validateNotNullParam(null);
    }

    @Test
    public void param_valid_validations_ok() {
        serviceParam.validateValidParam(new Pojo(Pojo.State.VALID));
    }


    @Test(expected = ValidationException.class)
    public void param_valid_validations_are_well_intercepted() {
        serviceParam.validateValidParam(new Pojo(Pojo.State.INVALID));
    }

    @Test
    public void not_null_return_validations_ok() {
        serviceReturnType.validateNotNullReturn("");
    }

    @Test(expected = ValidationException.class)
    public void not_null_return_validations_are_well_intercepted() {
        serviceReturnType.validateNotNullReturn(null);
    }

    @Test
    public void valid_return_validations_ok() {
        serviceReturnType.validateValidReturn(Pojo.State.VALID);
    }

    @Test(expected = ValidationException.class)
    public void valid_return_validations_are_well_intercepted() {
        serviceReturnType.validateValidReturn(Pojo.State.INVALID);
    }

}
