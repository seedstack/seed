/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.fixtures.validation;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.seedstack.seed.Application;

public class CheckCaseValidator implements ConstraintValidator<CheckCase, String> {
    private final Application application;
    private CaseMode caseMode;

    @Inject
    public CheckCaseValidator(Application application) {
        this.application = application;
    }

    @Override
    public void initialize(CheckCase constraintAnnotation) {
        this.caseMode = constraintAnnotation.value();
        if (application == null) {
            throw new IllegalStateException("Custom constraint validator is not injectable");
        }
    }

    @Override
    public boolean isValid(String object, ConstraintValidatorContext constraintContext) {
        if (application == null) {
            throw new IllegalStateException("Custom constraint validator is not injectable");
        }
        if (object == null) {
            return true;
        }

        if (caseMode == CaseMode.UPPER) {
            return object.equals(object.toUpperCase());
        } else {
            return object.equals(object.toLowerCase());
        }
    }
}