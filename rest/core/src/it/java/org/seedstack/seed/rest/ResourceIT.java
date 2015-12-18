/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.seedstack.seed.it.SeedITRunner;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.*;

/**
 * @author Pierre THIROUIN (pierre.thirouin@ext.inetpsa.com)
 */
@RunWith(SeedITRunner.class)
public class ResourceIT {

    @Inject
    private MyResource1 myResource1;
    @Inject
    private MyProvider1 myProvider1;

    @Test
    public void testResourcesAreInjected() throws Exception {
        assertThat(myResource1).isNotNull();
        assertThat(myResource1.getLogger()).isNotNull();
    }

    @Test
    public void testProvidersAreInjectedAsSingleton() throws Exception {
        assertThat(myProvider1).isNotNull();
        assertThat(myProvider1.getLogger()).isNotNull();
    }
}
