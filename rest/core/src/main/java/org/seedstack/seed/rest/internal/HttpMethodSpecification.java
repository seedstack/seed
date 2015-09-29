/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.internal;

import org.kametic.specifications.AbstractSpecification;

import javax.ws.rs.HttpMethod;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
 */
public class HttpMethodSpecification extends AbstractSpecification<Method> {

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
