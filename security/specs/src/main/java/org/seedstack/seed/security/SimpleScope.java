/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security;

import org.seedstack.seed.security.spi.SecurityScope;

/**
 * A simple kind of scope that can be described as a String which matches in an all or nothing fashion (strict equality).
 *
 * @author yves.dautremay@mpsa.com
 * @author adrien.lauer@mpsa.com
 */
@SecurityScope("scope")
public class SimpleScope implements Scope {

    /**
     * The value of the simple scope
     */
    private final String value;

    /**
     * Constructor with simple scope as param
     *
     * @param value the string value of the simple scope
     */
    public SimpleScope(String value) {
        this.value = value;
    }

    /**
     * Checks if the current simple scope equals the verified simple scope.
     */
    @Override
    public boolean includes(Scope scope) {
        if (scope == null) {
            return true;
        }
        if (scope instanceof SimpleScope) {
            SimpleScope simpleScope = (SimpleScope) scope;
            return this.value.equals(simpleScope.value);
        }
        return false;
    }

    @Override
    public String getName() {
        Class<? extends SimpleScope> scopeClass = getClass();
        SecurityScope annotation = scopeClass.getAnnotation(SecurityScope.class);
        if (annotation != null) {
            return annotation.value();
        } else {
            return scopeClass.getSimpleName().toLowerCase();
        }
    }

    @Override
    public String getValue() {
        return this.value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SimpleScope simpleScope = (SimpleScope) o;

        return value.equals(simpleScope.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
