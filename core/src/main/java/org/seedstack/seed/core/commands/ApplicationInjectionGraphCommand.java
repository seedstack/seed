/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.commands;

import org.seedstack.seed.core.api.Application;
import org.seedstack.seed.core.spi.command.Command;
import org.seedstack.seed.core.spi.command.CommandDefinition;
import org.seedstack.seed.core.spi.command.Option;

import javax.inject.Inject;

/**
 * Command to retrieve application graph as a DOT-formatted string.
 *
 * @author adrien.lauer@mpsa.com
 */
@CommandDefinition(scope = "core", name = "graph", description = "Return the application graph as DOT")
public class ApplicationInjectionGraphCommand implements Command {
    @Option(name = "f", longName = "filter", mandatory = false, description = "The filtering regular expression", hasArgument = true)
    private String filter;

    @Inject
    private Application application;

    @Override
    public Object execute(Object object) throws Exception {
        return application.getInjectionGraph(filter);
    }
}
