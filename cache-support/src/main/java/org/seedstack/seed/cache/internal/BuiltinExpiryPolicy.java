/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.cache.internal;


import javax.cache.configuration.Factory;
import javax.cache.expiry.AccessedExpiryPolicy;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;
import javax.cache.expiry.EternalExpiryPolicy;
import javax.cache.expiry.ExpiryPolicy;
import javax.cache.expiry.ModifiedExpiryPolicy;
import javax.cache.expiry.TouchedExpiryPolicy;
import java.lang.reflect.InvocationTargetException;

enum BuiltinExpiryPolicy {
    TOUCHED(TouchedExpiryPolicy.class, true),
    ACCESSED(AccessedExpiryPolicy.class, true),
    CREATED(CreatedExpiryPolicy.class, true),
    ETERNAL(EternalExpiryPolicy.class, false),
    MODIFIED(ModifiedExpiryPolicy.class, true);

    private static final String FACTORY_OF = "factoryOf";
    private final Class<? extends ExpiryPolicy> expiryPolicyClass;
    private final boolean hasDuration;

    private BuiltinExpiryPolicy(Class<? extends ExpiryPolicy> expiryPolicyClass, boolean hasDuration) {
        this.expiryPolicyClass = expiryPolicyClass;
        this.hasDuration = hasDuration;
    }

    @SuppressWarnings("unchecked")
    Factory<ExpiryPolicy> getFactory(Duration duration) {
        try {
            if (hasDuration) {
                return (Factory<ExpiryPolicy>) this.expiryPolicyClass.getDeclaredMethod(FACTORY_OF, Duration.class).invoke(null, duration);
            } else {
                return (Factory<ExpiryPolicy>) this.expiryPolicyClass.getDeclaredMethod(FACTORY_OF).invoke(null);
            }
        } catch(Exception e) {
            throw new RuntimeException("Unable to create expiry policy " + expiryPolicyClass.getCanonicalName(), e);
        }
    }
}
