/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.internal.jsonhome;

import java.util.List;

/**
 * Represents the authorization required by the resource as defined in the
 * <a href="http://tools.ietf.org/html/draft-nottingham-json-home-03#section-4.9">IETF draft</a>.
 * <p>
 * For example:
 * </p>
 * <pre>
 * {
 *   "auth-req": [
 *     {
 *       "scheme": "Basic",
 *       "realms": ["private"]
 *     }
 *   ]
 * }
 * </pre>
 */
public class AuthorizationRequired {

    private String scheme;

    private List<String> realms;

    /**
     * Constructor.
     *
     * @param scheme the HTTP authentication scheme
     * @param realms the realms identifying the protection spaces the resource is member of
     */
    public AuthorizationRequired(String scheme, List<String> realms) {
        this.scheme = scheme;
        this.realms = realms;
    }

    /**
     * Returns the HTTP authentication scheme.
     *
     * @return the scheme
     */
    public String getScheme() {
        return scheme;
    }

    /**
     * Returns the realms identifying the protection spaces the resource is member of.
     *
     * @return the list of realms
     */
    public List<String> getRealms() {
        return realms;
    }
}
