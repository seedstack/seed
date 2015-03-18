/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.cli.api;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Apply this annotation on a test method or a test class to execute SEED with the specified command-line. Standard
 * SEED startup through SeedITRunner is disabled for the whole class test. Only methods with this annotation (or all
 * method if it is applied on the class) will start a SEED environment.
 *
 * @author epo.jemba@ext.mpsa.com
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface WithCommandLine {
    /**
     * @return the array of command line arguments
     */
	String[] value();

    /**
     * @return the expected return code
     */
	int expectedExitCode() default 0;
}
