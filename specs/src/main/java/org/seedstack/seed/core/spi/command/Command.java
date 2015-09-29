/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.spi.command;

/**
 * Defines an action that can be executed by a command.
 *
 * @param <T> The return parameter of the execute method.
 * @author adrien.lauer@mpsa.com
 */
public interface Command<T> {
    /**
     * The action code to be executed.
     *
     * @param object input object of the command.
     * @return the output object of the command.
     * @throws Exception if an error occurs during execution.
     */
    T execute(Object object) throws Exception;
}
