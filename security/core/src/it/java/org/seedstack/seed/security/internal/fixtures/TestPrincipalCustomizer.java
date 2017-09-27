/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.security.internal.fixtures;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.seedstack.seed.security.PrincipalCustomizer;
import org.seedstack.seed.security.internal.realms.ConfigurationRealm;
import org.seedstack.seed.security.principals.PrincipalProvider;
import org.seedstack.seed.security.principals.SimplePrincipalProvider;

public class TestPrincipalCustomizer implements PrincipalCustomizer<ConfigurationRealm> {

    @Override
    public Class<ConfigurationRealm> supportedRealm() {
        return ConfigurationRealm.class;
    }

    @Override
    public Collection<PrincipalProvider<?>> principalsToAdd(PrincipalProvider<?> identity,
            Collection<PrincipalProvider<?>> realmPrincipals) {
        List<PrincipalProvider<?>> principals = new ArrayList<>();
        principals.add(new SimplePrincipalProvider("foo", "bar"));
        return principals;
    }

}
