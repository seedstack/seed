/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.rest.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.function.Predicate;
import javax.ws.rs.HttpMethod;

class HttpMethodPredicate implements Predicate<Method> {
    static final HttpMethodPredicate INSTANCE = new HttpMethodPredicate();

    private HttpMethodPredicate() {
        // no instantiation allowed
    }

    @Override
    public boolean test(Method candidate) {
        for (Annotation annotation : candidate.getDeclaredAnnotations()) {
            if (annotation.annotationType().getAnnotation(HttpMethod.class) != null) {
                return true;
            }
        }
        return false;
    }
}
