/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.rest.internal;

import java.lang.reflect.Method;
import org.kametic.specifications.AbstractSpecification;
import org.seedstack.seed.rest.Rel;

/**
 * The specification matches the method exposing a REST resource and declaring a relation type.
 * <p>
 * In order to be exposed the method should be:
 * </p>
 * <ol>
 * <li>meta annotated by {@link javax.ws.rs.HttpMethod};</li>
 * <li>annotated by {@link Rel};</li>
 * <li>If the annotated is not found on the method, the declaring class is checked.</li>
 * </ol>
 */
class RelSpecification extends AbstractSpecification<Method> {
    static RelSpecification INSTANCE = new RelSpecification();

    private RelSpecification() {
        // no instantiation allowed
    }

    @Override
    public boolean isSatisfiedBy(Method method) {
        if (!HttpMethodSpecification.INSTANCE.isSatisfiedBy(method)) {
            return false;
        }
        Rel rootRel = method.getDeclaringClass().getAnnotation(Rel.class);
        Rel rel = method.getAnnotation(Rel.class);
        return rootRel != null || rel != null;
    }
}
