/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.api;

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
    private String value;

    /**
     * Create a scope with an empty value.
     */
    public SimpleScope() {
        this.value = "";
    }

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
    public String getValue() {
        return this.value;
    }

    @Override
    public void setValue(String value) {
        this.value = value;
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
