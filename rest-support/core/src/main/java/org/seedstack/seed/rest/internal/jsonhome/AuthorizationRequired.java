/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.internal.jsonhome;

import java.util.List;

/**
 * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
 */
public class AuthorizationRequired {

    private String scheme;

    private List<String> realms;

    public AuthorizationRequired(String scheme, List<String> realms) {
        this.scheme = scheme;
        this.realms = realms;
    }

    public String getScheme() {
        return scheme;
    }

    public List<String> getRealms() {
        return realms;
    }
}
