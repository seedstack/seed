/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.web.fixtures.servlet;

import java.util.concurrent.atomic.AtomicBoolean;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class TestListener implements ServletContextListener {
    private static final AtomicBoolean called = new AtomicBoolean(false);

    public static boolean hasBeenCalled() {
        return called.get();
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        called.set(true);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }
}
