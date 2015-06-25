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
import org.seedstack.seed.rest.api.Rel;

import java.lang.reflect.Method;

/**
 * The specification matches the method which should be exposed as JSON-HOME resources.
 * In order to be exposed the method should be annotated by {@link org.seedstack.seed.rest.api.Rel}
 * with {@code expose=true}. If the annotated is not found the declaring class is checked.
 *
 * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
 */
public class JsonHomeSpecification extends AbstractSpecification<Method> {

    @Override
    public boolean isSatisfiedBy(Method method) {
        Rel rootRel = method.getDeclaringClass().getAnnotation(Rel.class);
        Rel rel = method.getAnnotation(Rel.class);
        if (rel != null) {
            return rel.expose();
        } else if (rootRel != null) {
            return rootRel.expose();
        }
        return false;
    }
}
