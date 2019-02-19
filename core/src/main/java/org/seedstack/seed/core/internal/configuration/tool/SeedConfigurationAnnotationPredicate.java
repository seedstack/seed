/*
 * Copyright © 2013-2021, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.configuration.tool;

import org.seedstack.seed.Configuration;
import org.seedstack.shed.reflect.AnnotationPredicates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Predicate;

/**
 * Matches all classes containing fields annotated by {@link org.seedstack.seed.Configuration}.
 * It also matches classes extending or implementing a class/interface with a Configuration-annotated field.
 * <p>
 */
class SeedConfigurationAnnotationPredicate implements Predicate<Class<?>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SeedConfigurationAnnotationPredicate.class);

    static final SeedConfigurationAnnotationPredicate INSTANCE = new SeedConfigurationAnnotationPredicate();

    private SeedConfigurationAnnotationPredicate() {
        // not instantiation allowed
    }

    @Override
    public boolean test(Class<?> candidate) {
        try {
            return AnnotationPredicates.atLeastOneFieldAnnotatedWith(Configuration.class, true)
                    .test(candidate);
        } catch (NoClassDefFoundError e) {
            LOGGER.trace("Unable to analyze fields in class {}", candidate.getName(), e);
            return false;
        }
    }
}
