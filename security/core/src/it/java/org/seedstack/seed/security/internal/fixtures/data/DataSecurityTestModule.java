/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.internal.fixtures.data;

import com.google.inject.AbstractModule;
import org.seedstack.seed.Install;

@Install
public class DataSecurityTestModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(DummyService.class).to(DummyServiceInternal.class);
    }
}
