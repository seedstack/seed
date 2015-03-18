/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.cli.spi;

import java.util.concurrent.Callable;

/**
 * This interface encapsulates the behaviour that will be handled by the SeedRunner.
 * 
 * @author epo.jemba@ext.mpsa.com
 */
public interface CommandLineHandler extends Callable<Integer> {
    /**
     * @return the name of the command line handler.
     */
	String name();
}
