/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web.internal.servlet;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import java.util.List;
import org.seedstack.seed.web.spi.FilterDefinition;
import org.seedstack.seed.web.spi.ListenerDefinition;
import org.seedstack.seed.web.spi.ServletDefinition;

class WebServletModule extends AbstractModule {
    private final List<FilterDefinition> filterDefinitions;
    private final List<ServletDefinition> servletDefinitions;
    private final List<ListenerDefinition> listenerDefinitions;

    WebServletModule(List<FilterDefinition> filterDefinitions, List<ServletDefinition> servletDefinitions,
            List<ListenerDefinition> listenerDefinitions) {
        this.filterDefinitions = filterDefinitions;
        this.servletDefinitions = servletDefinitions;
        this.listenerDefinitions = listenerDefinitions;
    }

    @Override
    protected void configure() {
        for (FilterDefinition filterDefinition : filterDefinitions) {
            bind(filterDefinition.getFilterClass()).in(Scopes.SINGLETON);
        }

        for (ServletDefinition servletDefinition : servletDefinitions) {
            bind(servletDefinition.getServletClass()).in(Scopes.SINGLETON);
        }

        for (ListenerDefinition listenerDefinition : listenerDefinitions) {
            bind(listenerDefinition.getListenerClass()).in(Scopes.SINGLETON);
        }
    }
}
