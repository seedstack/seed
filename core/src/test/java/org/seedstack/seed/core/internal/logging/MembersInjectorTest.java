/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.logging;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;


/**
 * LoggingMembersInjector Test
 */
public class MembersInjectorTest {
    private Logger log1;

    private final static Logger logger = LoggerFactory.getLogger(MembersInjectorTest.class);

    @Test
    public void injectMembersTest() throws Exception {
        Set<Field> fields = new HashSet<>();
        fields.add(this.getClass().getDeclaredField("log1"));
        LoggingMembersInjector<MembersInjectorTest> loggingMembersInjector1 = new LoggingMembersInjector<>(fields);
        loggingMembersInjector1.injectMembers(this);

        Assertions.assertThat(logger).isNotNull();
        Assertions.assertThat(log1).isSameAs(logger);
    }
}
