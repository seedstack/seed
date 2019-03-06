/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.internal.validation;

import com.google.inject.Injector;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.api.plugin.request.ClasspathScanRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ValidatorFactory;
import org.seedstack.seed.core.internal.AbstractSeedPlugin;
import org.seedstack.seed.core.internal.init.ValidationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This plugin handles validation through jsr303 and jsr349.
 */
public class ValidationPlugin extends AbstractSeedPlugin {
    private static final Logger LOGGER = LoggerFactory.getLogger(ValidationPlugin.class);
    private final Set<Class<? extends ConstraintValidator>> constraintValidators = new HashSet<>();
    private final ValidationManager.ValidationLevel level = ValidationManager.get().getValidationLevel();
    @Inject
    private Injector injector;

    @Override
    public String name() {
        return "validation";
    }

    @Override
    public Collection<ClasspathScanRequest> classpathScanRequests() {
        if (isValidationEnabled()) {
            return classpathScanRequestBuilder()
                    .specification(ConstraintValidatorSpecification.INSTANCE)
                    .build();
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    protected InitState initialize(InitContext initContext) {
        if (isValidationEnabled()) {
            Collection<Class<?>> constraintValidatorCandidates = initContext.scannedTypesBySpecification()
                    .get(ConstraintValidatorSpecification.INSTANCE);
            for (Class<?> candidate : constraintValidatorCandidates) {
                if (ConstraintValidator.class.isAssignableFrom(candidate)) {
                    LOGGER.debug("Detected constraint validator {}", candidate.getCanonicalName());
                    constraintValidators.add(candidate.asSubclass(ConstraintValidator.class));
                }
            }
            LOGGER.info("Bean validation is enabled at level {}", level);
        } else {
            LOGGER.info("Bean validation is disabled");
        }
        return InitState.INITIALIZED;
    }

    @Override
    public Object nativeUnitModule() {
        if (isValidationEnabled()) {
            return new ValidationModule(level, constraintValidators);
        } else {
            return null;
        }
    }

    @Override
    public void stop() {
        if (isValidationEnabled() && isValidation11Supported()) {
            try {
                injector.getInstance(ValidatorFactory.class).close();
            } catch (Exception e) {
                LOGGER.warn("Unable to close ValidatorFactory", e);
            }
        }
    }

    private boolean isValidationEnabled() {
        return level != ValidationManager.ValidationLevel.NONE;
    }

    private boolean isValidation11Supported() {
        return ValidationManager.get().getValidationLevel().compareTo(ValidationManager.ValidationLevel.LEVEL_1_1) >= 0;
    }
}
