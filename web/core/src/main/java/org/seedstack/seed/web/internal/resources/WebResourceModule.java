/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web.internal.resources;

import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.servlet.ServletModule;
import org.seedstack.seed.web.WebResourceResolver;
import org.seedstack.seed.web.WebResourceResolverFactory;

import javax.inject.Singleton;

@WebResourceConcern
class WebResourceModule extends ServletModule {
    @Override
    protected void configureServlets() {
        install(new FactoryModuleBuilder()
                .implement(WebResourceResolver.class, WebResourceResolverImpl.class)
                .build(WebResourceResolverFactory.class)
        );

        bind(WebResourceFilter.class).in(Singleton.class);
        filter("/*").through(WebResourceFilter.class);
    }
}
