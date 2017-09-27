/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.web.internal.resources;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import org.seedstack.seed.web.WebResourceResolver;
import org.seedstack.seed.web.WebResourceResolverFactory;

class WebResourcesModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(WebResourcesFilter.class).in(Scopes.SINGLETON);

        install(new FactoryModuleBuilder()
                .implement(WebResourceResolver.class, WebResourcesResolverImpl.class)
                .build(WebResourceResolverFactory.class)
        );
    }
}
