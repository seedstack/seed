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

/**
 * A scope describing an LDAP domain as a simple String
 *
 * @author yves.dautremay@mpsa.com
 */
public class Domain implements Scope {

    /**
     * The name of the domain
     */
    private String name;

    /**
     * Constructor with domain as param
     *
     * @param name
     *         the domain
     */
    public Domain(String name) {
        this.name = name;
    }

    /**
     * Checks if the current domain equals the verified domain.
     */
    @Override
    public boolean includes(Scope scope) {
        if (scope == null) {
            return true;
        }
        if (scope instanceof Domain) {
            Domain domain = (Domain) scope;
            return this.name.equals(domain.getName());
        }
        return false;
    }

    @Override
    public String getDescription() {
        return this.name;
    }

    /**
     * Getter domain
     *
     * @return the domain
     */
    public String getName() {
        return this.name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Domain domain = (Domain) o;

        return name.equals(domain.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
