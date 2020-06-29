/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.rest.internal;

import java.lang.reflect.Method;
import java.util.function.Predicate;
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
class JsonHomePredicate implements Predicate<Method> {
    static JsonHomePredicate INSTANCE = new JsonHomePredicate();

    private JsonHomePredicate() {
        // no instantiation allowed
    }

    @Override
    public boolean test(Method method) {
        if (!HttpMethodPredicate.INSTANCE.test(method)) {
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
