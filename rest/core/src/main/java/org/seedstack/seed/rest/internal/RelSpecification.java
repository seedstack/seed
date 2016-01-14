/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.internal;

import org.kametic.specifications.AbstractSpecification;
import org.seedstack.seed.rest.Rel;

import java.lang.reflect.Method;

/**
 * The specification matches the method exposing a REST resource and declaring a relation type.
 * <p>
 * In order to be exposed the method should be:
 * </p>
 * <ol>
 *   <li>meta annotated by {@link javax.ws.rs.HttpMethod};</li>
 *   <li>annotated by {@link Rel};</li>
 *   <li>If the annotated is not found on the method, the declaring class is checked.</li>
 * </ol>
 *
 * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
 */
public class RelSpecification extends AbstractSpecification<Method> {

    private static final HttpMethodSpecification HTTP_METHOD_SPECIFICATION = new HttpMethodSpecification();

    @Override
    public boolean isSatisfiedBy(Method method) {
        if (!HTTP_METHOD_SPECIFICATION.isSatisfiedBy(method)) {
            return false;
        }

        Rel rootRel = method.getDeclaringClass().getAnnotation(Rel.class);
        Rel rel = method.getAnnotation(Rel.class);
        return rootRel != null || rel != null;
    }
}
