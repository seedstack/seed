/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.validation;

import com.google.inject.spi.ProvisionListener;
import java.util.Set;
import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

class StaticValidationProvisionListener implements ProvisionListener {
    @Inject
    private Validator validator;

    @Override
    public <A> void onProvision(ProvisionInvocation<A> provision) {
        A provisioned = provision.provision();
        Set<ConstraintViolation<A>> constraintViolations = validator.validate(provisioned);
        if (!constraintViolations.isEmpty()) {
            throw new VerboseConstraintViolationException(constraintViolations);
        }
    }
}
