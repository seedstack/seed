/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.commands;

import org.seedstack.seed.core.api.Application;
import org.seedstack.seed.core.spi.command.Command;
import org.seedstack.seed.core.spi.command.CommandDefinition;
import org.apache.commons.configuration.ConfigurationConverter;

import javax.inject.Inject;

/**
 * The command to retrieve global application configuration.
 *
 * @author adrien.lauer@mpsa.com
 */
@CommandDefinition(scope = "core", name = "conf", description = "Return the application configuration as a map")
public class ApplicationConfigurationCommand implements Command {
    @Inject
    private Application application;

    @Override
    public Object execute(Object object) throws Exception {
        return ConfigurationConverter.getMap(application.getConfiguration());
    }
}
