/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.internal.data;

import java.io.InputStream;
import java.io.OutputStream;
import javax.inject.Inject;
import org.seedstack.seed.DataManager;
import org.seedstack.seed.command.CommandDefinition;
import org.seedstack.seed.command.Option;
import org.seedstack.seed.command.StreamCommand;

/**
 * Command to export data out of the application.
 */
@CommandDefinition(scope = "core", name = "export", description = "Export application data")
public class DataExportCommand implements StreamCommand {
    @Inject
    DataManager dataManager;
    @Option(name = "g", longName = "group", mandatory = false, description = "The group of data to export",
            hasArgument = true)
    private String group;
    @Option(name = "s", longName = "set", mandatory = false, description = "The name of the data set of group to "
            + "export", hasArgument = true)
    private String set;

    @Override
    public void execute(InputStream inputStream, OutputStream outputStream, OutputStream errorStream) {
        if (group != null) {
            if (set != null) {
                dataManager.exportData(outputStream, group, set);
            } else {
                dataManager.exportData(outputStream, group);
            }
        } else {
            dataManager.exportData(outputStream);
        }
    }

    @Override
    public Object execute(Object object) throws Exception {
        throw new IllegalStateException("This command cannot be invoked in interactive mode");
    }
}
