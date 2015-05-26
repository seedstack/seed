/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.jms.spi;


import javax.jms.ExceptionListener;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;

/**
 * Interface for message pollers.
 *
 * @author redouane.loulou@ext.mpsa.com
 * @author adrien.lauer@mpsa.com
 */
public interface MessagePoller {
    void setSession(Session session);

    void setMessageConsumer(MessageConsumer messageConsumer);

    void setExceptionListener(ExceptionListener exceptionListener);

    void setMessageListener(MessageListener messageListener);

    void start();

    void stop();

}
