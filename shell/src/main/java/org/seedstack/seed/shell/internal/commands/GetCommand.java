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

import java.lang.reflect.InvocationTargetException;

/**
 * This command return the property value of input bean.
 *
 * @author adrien.lauer@mpsa.com
 */
@CommandDefinition(scope = "", name = "get", description = "Get a property on input object")
public class GetCommand implements Command {
    @Argument(index = 0, description = "The property to get")
    private String property;

    @Override
    public Object execute(Object object) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        if (object == null) {
            return null;
        }

        return BeanUtils.getProperty(object, property);
    }
}
