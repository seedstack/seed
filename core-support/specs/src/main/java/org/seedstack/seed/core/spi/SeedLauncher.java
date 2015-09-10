/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.spi;

/**
 * This interface defines a method that can launch a Seed application.
 * It must be declared as a {@link java.util.ServiceLoader} service in META-INF/services to be detected.
 *
 * @author adrien.lauer@gmail.com
 */
public interface SeedLauncher {
    /**
     * The method that launches the Seed application.
     *
     * @param args arguments of the Seed application.
     * @return the return code.
     */
    int launch(String[] args) throws Exception;
}
