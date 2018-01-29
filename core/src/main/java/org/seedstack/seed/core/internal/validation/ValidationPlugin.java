/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.internal.validation;

import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.api.plugin.request.ClasspathScanRequest;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.validation.ConstraintValidator;
import javax.validation.ValidatorFactory;
import org.seedstack.seed.core.SeedRuntime;
import org.seedstack.seed.core.internal.AbstractSeedPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This plugin handles validation through jsr303 and jsr349.
 */
public class ValidationPlugin extends AbstractSeedPlugin {
    private static final Logger LOGGER = LoggerFactory.getLogger(ValidationPlugin.class);
    private final Set<Class<? extends ConstraintValidator>> constraintValidators = new HashSet<>();
    private ValidatorFactory globalValidatorFactory = null;

    @Override
    public String name() {
        return "validation";
    }

    @Override
    protected void setup(SeedRuntime seedRuntime) {
        globalValidatorFactory = seedRuntime.getValidatorFactory();
    }

    @Override
    public Collection<ClasspathScanRequest> classpathScanRequests() {
        return classpathScanRequestBuilder()
                .specification(ConstraintValidatorSpecification.INSTANCE)
                .build();
    }

    @Override
    protected InitState initialize(InitContext initContext) {
        Collection<Class<?>> constraintValidatorCandidates = initContext.scannedTypesBySpecification()
                .get(ConstraintValidatorSpecification.INSTANCE);
        for (Class<?> candidate : constraintValidatorCandidates) {
            if (ConstraintValidator.class.isAssignableFrom(candidate)) {
                LOGGER.debug("Detected constraint validator {}", candidate.getCanonicalName());
                constraintValidators.add(candidate.asSubclass(ConstraintValidator.class));
            }
        }
        LOGGER.debug("Detected {} constraint validator(s)", constraintValidators.size());
        return InitState.INITIALIZED;
    }

    @Override
    public Object nativeUnitModule() {
        return new ValidationModule(globalValidatorFactory, constraintValidators);
    }
}
