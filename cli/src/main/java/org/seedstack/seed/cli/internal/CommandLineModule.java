/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.cli.internal;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import org.seedstack.seed.cli.api.CommandLineHandler;

import java.util.Map;

/**
 * @author epo.jemba@ext.mpsa.com
 * @author adrien.lauer@mpsa.com
 */
class CommandLineModule extends AbstractModule {
    private final Map<String, Class<? extends CommandLineHandler>> cliHandlers;

    CommandLineModule(Map<String, Class<? extends CommandLineHandler>> cliHandlers) {
        this.cliHandlers = cliHandlers;
    }

    @Override
    protected void configure() {
        bind(CommandLineITRule.class);

        for (Map.Entry<String, Class<? extends CommandLineHandler>> cliHandlerEntry : cliHandlers.entrySet()) {
            bind(CommandLineHandler.class).annotatedWith(Names.named(cliHandlerEntry.getKey())).to(cliHandlerEntry.getValue());
        }
    }
}
