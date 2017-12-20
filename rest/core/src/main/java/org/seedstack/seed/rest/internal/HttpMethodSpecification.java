/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.rest.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import javax.ws.rs.HttpMethod;
import org.kametic.specifications.AbstractSpecification;

class HttpMethodSpecification extends AbstractSpecification<Method> {
    static final HttpMethodSpecification INSTANCE = new HttpMethodSpecification();

    private HttpMethodSpecification() {
        // no instantiation allowed
    }

    @Override
    public boolean isSatisfiedBy(Method candidate) {
        for (Annotation annotation : candidate.getDeclaredAnnotations()) {
            if (annotation.annotationType().getAnnotation(HttpMethod.class) != null) {
                return true;
            }
        }
        return false;
    }
}
