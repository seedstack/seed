/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.command;

import org.seedstack.seed.DataManager;
import org.seedstack.seed.spi.command.CommandDefinition;
import org.seedstack.seed.spi.command.Option;
import org.seedstack.seed.spi.command.StreamCommand;

import javax.inject.Inject;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Command to import data in the application.
 *
 * @author adrien.lauer@mpsa.com
 */
@CommandDefinition(scope = "core", name = "import", description = "Import application data")
public class DataImportCommand implements StreamCommand {
    @Option(name = "c", longName = "clear", mandatory = false, description = "Clear existing data if import is succeeding", hasArgument = false)
    private boolean clear;

    @Inject
    DataManager dataManager;

    @Override
    public void execute(InputStream inputStream, OutputStream outputStream, OutputStream errorStream) {
        dataManager.importData(inputStream, null, null, clear);
    }

    @Override
    public Object execute(Object object) throws Exception {
        throw new IllegalStateException("This command cannot be invoked in interactive mode");
    }
}
