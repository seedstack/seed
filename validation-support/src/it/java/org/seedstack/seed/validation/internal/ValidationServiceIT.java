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
import org.seedstack.seed.validation.api.ValidationService;
import org.seedstack.seed.validation.internal.pojo.Pojo;
import org.seedstack.seed.validation.internal.pojo.PojoWithDeepValidation;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.seedstack.seed.it.SeedITRunner;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SeedITRunner.class)
public class ValidationServiceIT {

    @Inject
    ValidationService validationService;

    @Test
    public void validation_service_is_well_injected() {
        assertThat(validationService).isNotNull();
    }

    @Test
    public void validation_service_work_nominally() {
        try {
            validationService.staticallyHandle(new Pojo(Pojo.State.INVALID));

            Assertions.failBecauseExceptionWasNotThrown(ValidationException.class);
        } catch (ValidationException validationException) {
            validationException.printStackTrace();
            Set<ConstraintViolation<?>> constraintViolations = validationException.get(ValidationService.JAVAX_VALIDATION_CONSTRAINT_VIOLATIONS);

            assertThat(constraintViolations.size()).isEqualTo(3);
        }
    }

    @Test
    public void validation_on_cascade() {
        try {
            validationService.staticallyHandle(new PojoWithDeepValidation());

            Assertions.failBecauseExceptionWasNotThrown(ValidationException.class);
        } catch (ValidationException validationException) {
            validationException.printStackTrace();
            Set<ConstraintViolation<?>> constraintViolations = validationException.get(ValidationService.JAVAX_VALIDATION_CONSTRAINT_VIOLATIONS);

            assertThat(constraintViolations.size()).isEqualTo(4);
        }
    }
}
