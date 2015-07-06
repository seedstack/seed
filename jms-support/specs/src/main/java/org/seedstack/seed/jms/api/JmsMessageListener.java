/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.jms.api;

import org.seedstack.seed.jms.spi.MessagePoller;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotations marks a JMS message listener that will be invoked when a message arrives.
 *
 * @author emmanuel.vinel@mpsa.com
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE})
@Inherited
public @interface JmsMessageListener  {
    /**
     * @return name of the configured JMS connection to use.
     */
    String connection() default "default";

    /**
     * @return the type of the JMS destination to listen to.
     */
    DestinationType destinationType();

    /**
     * @return the name of the JMS destination to listen to.
     */
    String destinationName();

    /**
     * @return an optional selector to only retrieve messages that matches it.
     */
    String selector() default "";

    /**
     * @return an optional {@link org.seedstack.seed.jms.spi.MessagePoller} to retrieve messages via receive() instead
     * of asynchronous delivery.
     */
    Class<? extends MessagePoller> poller()[] default {};
}
