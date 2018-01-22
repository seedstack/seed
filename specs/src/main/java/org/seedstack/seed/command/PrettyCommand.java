/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.command;

/**
 * This interface can be implemented by commands that are able to prettify their output object as a string.
 *
 * @param <T> The return parameter of the {@link Command#execute(Object)} method.
 */
public interface PrettyCommand<T> extends Command<T> {
    /**
     * This method is called with the return value of {@link Command#execute(Object)}
     * as parameter if the execution context allows pretty output.
     *
     * @param object the return value of {@link Command#execute(Object)}
     * @return a formatted string for pretty output.
     * @throws Exception if the prettifying process throws an error.
     */
    String prettify(T object) throws Exception;
}
