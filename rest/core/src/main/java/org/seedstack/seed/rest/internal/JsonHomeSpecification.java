/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
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
 * The specification matches HTTP methods which should be exposed as JSON-HOME resources.
 * <p>
 * In order to be exposed the method should be:
 * </p>
 * <ol>
 * <li>meta annotated by {@link javax.ws.rs.HttpMethod};</li>
 * <li>annotated by {@link Rel} with {@code home=true};</li>
 * <li>If the annotated is not found on the method, the declaring class is checked.</li>
 * </ol>
 */
class JsonHomeSpecification extends AbstractSpecification<Method> {
    static JsonHomeSpecification INSTANCE = new JsonHomeSpecification();

    private JsonHomeSpecification() {
        // no instantiation allowed
    }

    @Override
    public boolean isSatisfiedBy(Method method) {
        if (!HttpMethodSpecification.INSTANCE.isSatisfiedBy(method)) {
            return false;
        }
        Rel rootRel = method.getDeclaringClass().getAnnotation(Rel.class);
        Rel rel = method.getAnnotation(Rel.class);
        if (rel != null) {
            return rel.home();
        } else if (rootRel != null) {
            return rootRel.home();
        }
        return false;
    }
}
