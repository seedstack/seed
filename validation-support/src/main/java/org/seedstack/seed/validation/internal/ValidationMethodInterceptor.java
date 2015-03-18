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


import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.seedstack.seed.validation.api.ValidationService;

import static org.seedstack.seed.core.utils.SeedReflectionUtils.cleanProxy;


class ValidationMethodInterceptor implements MethodInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger(ValidationMethodInterceptor.class);
    private final ValidationService validationService;

    ValidationMethodInterceptor(ValidationService validationService) {
        this.validationService = validationService;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {

        try {
            LOGGER.debug("Validation of {}", cleanProxy(invocation.getClass()));
            return this.validationService.dynamicallyHandleAndProceed(invocation);
        } finally {
            LOGGER.debug("End of validation of {}", cleanProxy(invocation.getClass()));
        }
    }

}
