/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.jms;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.seedstack.seed.it.SeedITRunner;
import org.seedstack.seed.jms.fixtures.TestMessageListener1;
import org.seedstack.seed.jms.fixtures.TestSender1;
import org.seedstack.seed.jms.fixtures.TestSender2;
import org.seedstack.seed.jms.fixtures.TestMessageListener2;

import javax.jms.Connection;
import javax.jms.JMSException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.fail;

@RunWith(SeedITRunner.class)
public class JmsBaseIT {
    @Inject
    TestSender1 testSender1;

    @Inject
    TestSender2 testSender2;

    @Inject
    Injector injector;

    public static CountDownLatch managed = new CountDownLatch(1);
    public static CountDownLatch unmanaged = new CountDownLatch(1);
    public static String textManaged = null;
    public static String textUnmanaged = null;

    /**
     * TestSender1 and TestMessageListener1.
     */
    @Test
    public void managed_send_and_receive_is_working() throws JMSException {
        testSender1.send("MANAGED");

        try {
            managed.await(1, TimeUnit.SECONDS);

            Assertions.assertThat(textManaged).isEqualTo("MANAGED");
        } catch (InterruptedException e) {
            fail("Thread interrupted");
        }
    }

    /**
     * TestSender2 and TestMessageListener2.
     */
    @Test
    public void unmanaged_send_and_receive_is_working() throws JMSException {

        testSender2.send("UNMANAGED");

        try {
            unmanaged.await(1, TimeUnit.SECONDS);

            Assertions.assertThat(textUnmanaged).isEqualTo("UNMANAGED");
        } catch (InterruptedException e) {
            fail("Thread interrupted");
        }
    }

    @Test
    public void connections_are_singletons() throws JMSException {
        Assertions.assertThat(injector.getInstance(Key.get(Connection.class, Names.named("connection1")))).isSameAs(injector.getInstance(Key.get(Connection.class, Names.named("connection1"))));
    }
}
