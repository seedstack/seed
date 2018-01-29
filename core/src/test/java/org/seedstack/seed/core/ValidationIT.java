/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.inject.Injector;
import com.google.inject.ProvisionException;
import javax.inject.Inject;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.seedstack.seed.core.fixtures.validation.CustomPojo;
import org.seedstack.seed.core.fixtures.validation.FieldValidationKO;
import org.seedstack.seed.core.fixtures.validation.FieldValidationOK;
import org.seedstack.seed.core.fixtures.validation.ParamReturnType;
import org.seedstack.seed.core.fixtures.validation.ParamValidation;
import org.seedstack.seed.core.fixtures.validation.Pojo;
import org.seedstack.seed.core.fixtures.validation.WithoutValidation;
import org.seedstack.seed.testing.junit4.SeedITRunner;

@RunWith(SeedITRunner.class)
public class ValidationIT {
    @Inject
    private ValidatorFactory validatorFactory;
    @Inject
    private Validator validator;
    @Inject
    private ParamValidation paramValidation;
    @Inject
    private ParamReturnType paramReturnType;
    @Inject
    private WithoutValidation withoutValidation;
    @Inject
    private FieldValidationOK fieldValidationOK;
    @Inject
    private Injector injector;

    @Test
    public void injections() {
        assertThat(validatorFactory).isNotNull();
        assertThat(validator).isNotNull();

        assertThat(paramValidation).isNotNull();
        assertThat(fieldValidationOK).isNotNull();
        assertThat(paramReturnType).isNotNull();
        assertThat(withoutValidation).isNotNull();
    }

    @Test
    public void fieldValidationsOk() {
        assertThat(fieldValidationOK.getParam()).isNotNull();
    }

    @Test(expected = ProvisionException.class)
    public void fieldNotNullValidationsAreWellIntercepted() {
        injector.getInstance(FieldValidationKO.class);
    }

    @Test
    public void paramNotNullValidationsOk() {
        paramValidation.validateNotNullParam("");
    }

    @Test(expected = ConstraintViolationException.class)
    public void paramNotNullValidationsAreWellIntercepted() {
        paramValidation.validateNotNullParam(null);
    }

    @Test
    public void paramValidValidationsOk() {
        paramValidation.validateValidParam(new Pojo(Pojo.State.VALID));
    }

    @Test(expected = ConstraintViolationException.class)
    public void paramValidValidationsAreWellIntercepted() {
        paramValidation.validateValidParam(new Pojo(Pojo.State.INVALID));
    }

    @Test
    public void notNullReturnValidationsOk() {
        paramReturnType.validateNotNullReturn("");
    }

    @Test(expected = ConstraintViolationException.class)
    public void notNullReturnValidationsAreWellIntercepted() {
        paramReturnType.validateNotNullReturn(null);
    }

    @Test
    public void validReturnValidationsOk() {
        paramReturnType.validateValidReturn(Pojo.State.VALID);
    }

    @Test(expected = ConstraintViolationException.class)
    public void validReturnValidationsAreWellIntercepted() {
        paramReturnType.validateValidReturn(Pojo.State.INVALID);
    }

    @Test
    public void custom_validator() {
        assertThat(validator.validate(new CustomPojo("abc")).size()).isEqualTo(1);
        assertThat(validator.validate(new CustomPojo("ABC")).size()).isEqualTo(0);
    }
}
