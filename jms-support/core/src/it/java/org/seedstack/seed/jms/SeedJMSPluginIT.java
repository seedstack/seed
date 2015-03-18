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
import org.seedstack.seed.it.SeedITRunner;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.jms.Connection;
import javax.jms.JMSException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.fail;

@RunWith(SeedITRunner.class)
public class SeedJMSPluginIT {

	@Inject
	MyManagedMessageSender myManagedMessageSender;

	@Inject
	MyMessageListener myMessageListener;

	@Inject
	MyUnManagedMessageSender myUnManagedMessageSender;

	@Inject
	MyMessageListener2 myMessageListener2;

	@Inject
	Injector injector;

    static CountDownLatch managed = new CountDownLatch(1);
    static CountDownLatch unmanaged = new CountDownLatch(1);
    static String textManaged = null;
	static String textUnManaged = null;

	@Test
	public void messageListeners_should_be_injectables() {
		Assertions.assertThat(myManagedMessageSender).isNotNull();
        Assertions.assertThat(myMessageListener).isNotNull();
        Assertions.assertThat(myMessageListener.logger).isNotNull();
        Assertions.assertThat(myMessageListener2).isNotNull();
        Assertions.assertThat(myMessageListener2.logger).isNotNull();
	}

    /**
     * MessageListener with transaction.
     */
	@Test
	public void sendManagedMsg() throws JMSException {
		myManagedMessageSender.send("MANAGED");

		try {
            managed.await(1, TimeUnit.SECONDS);

			Assertions.assertThat(textManaged).isEqualTo("MANAGED");
		} catch (InterruptedException e) {
			fail("Thread interrupted");
		}
	}

    /**
     * MessageListener without transaction.
     */
	@Test
	public void sendUnmanagedMsg() throws JMSException {

		myUnManagedMessageSender.send("UNMANAGED");

		try {
            unmanaged.await(1, TimeUnit.SECONDS);

			Assertions.assertThat(textUnManaged).isEqualTo("UNMANAGED");
		} catch (InterruptedException e) {
			fail("Thread interrupted");
		}
	}

	@Test
	public void connections_are_singletons() throws JMSException {
		Assertions.assertThat(injector.getInstance(Key.get(Connection.class, Names.named("connection1")))).isSameAs(injector.getInstance(Key.get(Connection.class, Names.named("connection1"))));
	}
}
