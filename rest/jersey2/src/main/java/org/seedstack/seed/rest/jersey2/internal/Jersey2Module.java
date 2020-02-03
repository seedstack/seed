/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.jersey2.internal;

import com.google.inject.AbstractModule;

class Jersey2Module extends AbstractModule {
    private SeedServletContainer seedServletContainer;

    Jersey2Module(SeedServletContainer seedServletContainer) {
        this.seedServletContainer = seedServletContainer;
    }

    @Override
    protected void configure() {
        bind(SeedServletContainer.class).toInstance(seedServletContainer);
    }
}
