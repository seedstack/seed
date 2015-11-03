/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed;

import org.seedstack.seed.spi.command.Argument;
import org.seedstack.seed.spi.command.CommandDefinition;
import org.seedstack.seed.spi.command.Command;
import org.seedstack.seed.spi.command.Option;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The command registry holds all command definitions and can be used to retrieve additional information on them.
 *
 * @author adrien.lauer@mpsa.com
 */
public interface CommandRegistry {

    /**
     * Retrieve list of all registered commands.
     * @return the list of fully qualified (scope:name) commands.
     */
    Set<String> getCommandList();

    /**
     * Retrieve the options list of a command.
     *
     * @param scope the command scope.
     * @param name the command name.
     * @return the list of {@link org.seedstack.seed.spi.command.Option}.
     */
    List<Option> getOptionsInfo(String scope, String name);


    /**
     * Retrieve the argument list of a command.
     *
     * @param scope the command scope.
     * @param name the command name.
     * @return the list of {@link org.seedstack.seed.spi.command.Argument}.
     */
    List<Argument> getArgumentsInfo(String scope, String name);

    /**
     * Retrieve the command definition
     *
     * @param scope the command scope.
     * @param name the command name.
     * @return the {@link org.seedstack.seed.spi.command.CommandDefinition} object.
     */
    CommandDefinition getCommandInfo(String scope, String name);

    /**
     * Instantiate a {@link org.seedstack.seed.spi.command.Command} object given a scope, a name and a list of arguments and options.
     *
     * @param scope the command scope.
     * @param name the command name.
     * @param argValues the argument values.
     * @param optionValues the option values.
     *
     * @return the {@link org.seedstack.seed.spi.command.Command} object, already initialized and injected with corresponding values.
     */
    Command createCommand(String scope, String name, List<String> argValues, Map<String, String> optionValues);
}
