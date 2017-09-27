/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.security.principals;

/**
 * A named principal represented as a string.
 */
public class SimplePrincipalProvider implements PrincipalProvider<String> {
    private final String name;
    private final String value;

    /**
     * Constructor
     *
     * @param name  name
     * @param value value
     */
    public SimplePrincipalProvider(String name, String value) {
        this.name = name;
        this.value = value;
    }

    /**
     * @return the name of the principal.
     */
    public String getName() {
        return name;
    }

    /**
     * @return the value of the principal.
     */
    public String getValue() {
        return value;
    }

    @Override
    public String getPrincipal() {
        return value;
    }
}
