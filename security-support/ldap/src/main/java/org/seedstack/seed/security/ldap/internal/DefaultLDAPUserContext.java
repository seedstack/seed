/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.ldap.internal;

import java.util.HashMap;
import java.util.Map;

import org.seedstack.seed.security.ldap.api.LDAPUserContext;

public class DefaultLDAPUserContext implements LDAPUserContext {

    private static final long serialVersionUID = -8670518172669970580L;

    private String dn;

    private Map<String, String> knownAttributes = new HashMap<String, String>();

    public DefaultLDAPUserContext(String dn) {
        this.dn = dn;
    }

    @Override
    public String getDn() {
        return dn;
    }

    Map<String, String> getKnownAttributes() {
        return knownAttributes;
    }

}
