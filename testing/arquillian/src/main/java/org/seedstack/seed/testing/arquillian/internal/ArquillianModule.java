/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.testing.arquillian.internal;

import com.google.inject.AbstractModule;
import org.seedstack.seed.Install;
import org.seedstack.shed.reflect.Classes;

@Install
class ArquillianModule extends AbstractModule {
    @Override
    protected void configure() {
        Classes.optional("org.jboss.arquillian.test.spi.TestEnricher")
                .ifPresent((c) ->
                        requestStaticInjection(InjectionTestEnricher.class)
                );
    }
}
