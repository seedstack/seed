/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * LoggingMembersInjector Test
 *
 * @author redouane.loulou@ext.mpsa.com
 */
public class LoggingMembersInjectorTest {

    private Logger log1;

    private final static Logger logger = LoggerFactory.getLogger(LoggingMembersInjectorTest.class);


    @Test
    public void injectMembersTest() throws Exception {
        LoggingMembersInjector<LoggingMembersInjectorTest> loggingMembersInjector1 = new LoggingMembersInjector<LoggingMembersInjectorTest>(this.getClass().getDeclaredField("log1"));
        loggingMembersInjector1.injectMembers(this);

        Assertions.assertThat(logger).isNotNull();
        Assertions.assertThat(log1).isSameAs(logger);
    }
}
