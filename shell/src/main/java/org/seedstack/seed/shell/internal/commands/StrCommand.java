/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.shell.internal.commands;

import org.apache.commons.beanutils.BeanUtils;
import org.seedstack.seed.spi.command.Argument;
import org.seedstack.seed.spi.command.Command;
import org.seedstack.seed.spi.command.CommandDefinition;

/**
 * This command stringify any input object.
 *
 * @author adrien.lauer@mpsa.com
 */
@CommandDefinition(scope = "", name = "str", description = "Return the argument string")
public class StrCommand implements Command {
    @Argument(index = 0, description = "The property to get", mandatory = false, defaultValue = "")
    private String string;

    @Override
    public Object execute(Object object) throws Exception {
        if (object != null) {
            return object.toString();
        }

        return BeanUtils.getProperty(object, string);
    }
}
