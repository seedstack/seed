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
 * This interface defines a Seed executable entry point. It must be declared as a {@link java.util.ServiceLoader} service
 * in META-INF/services to be detected.
 *
 * @author adrien.lauer@gmail.com
 */
public interface SeedRunnable {
    /**
     * The entry-point method that is executed upon startup.
     *
     * @param args the arguments.
     * @return the return code.
     */
    int run(String[] args) throws Exception;
}
