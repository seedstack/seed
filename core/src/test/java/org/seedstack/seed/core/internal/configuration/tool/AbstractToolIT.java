/*
 * Copyright © 2013-2024, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.configuration.tool;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.seedstack.seed.core.internal.configuration.tool.fixtures.ExitExceptionSecurityManager;
import org.seedstack.seed.testing.junit4.SeedITRunner;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

@RunWith(SeedITRunner.class)
public abstract class AbstractToolIT {

    final static ByteArrayOutputStream output = new ByteArrayOutputStream();

    private final static PrintStream savedOut = System.out;
    private static SecurityManager savedSecurityManager = System.getSecurityManager();

    @BeforeClass
    public static void beforeClass() {
        System.setOut(new PrintStream(output));
        System.setSecurityManager(new ExitExceptionSecurityManager());
    }

    @AfterClass
    public static void afterClass() {
        System.setOut(savedOut);
        System.setSecurityManager(savedSecurityManager);
    }

    @After
    public void afterTest() {
        output.reset();
    }
}