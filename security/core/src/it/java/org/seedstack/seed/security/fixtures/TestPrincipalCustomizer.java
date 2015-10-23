/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.fixtures;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.seedstack.seed.security.api.PrincipalCustomizer;
import org.seedstack.seed.security.api.principals.PrincipalProvider;
import org.seedstack.seed.security.api.principals.SimplePrincipalProvider;
import org.seedstack.seed.security.internal.realms.ConfigurationRealm;

public class TestPrincipalCustomizer implements PrincipalCustomizer<ConfigurationRealm> {

    @Override
    public Class<ConfigurationRealm> supportedRealm() {
        return ConfigurationRealm.class;
    }

    @Override
    public Collection<PrincipalProvider<?>> principalsToAdd(PrincipalProvider<?> identity, Collection<PrincipalProvider<?>> realmPrincipals) {
        List<PrincipalProvider<?>> principals = new ArrayList<PrincipalProvider<?>>();
        principals.add(new SimplePrincipalProvider("foo", "bar"));
        return principals;
    }

}
