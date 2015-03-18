/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.jms.internal;


import org.seedstack.seed.transaction.spi.TransactionalLink;

import javax.jms.Session;
import java.util.ArrayDeque;
import java.util.Deque;

class JmsSessionLink implements TransactionalLink<Session> {
    private final ThreadLocal<Deque<Session>> sessionThreadLocal;

    JmsSessionLink() {
        sessionThreadLocal = new ThreadLocal<Deque<Session>>() {
            @Override
            protected Deque<Session> initialValue() {
                return new ArrayDeque<Session>();
            }
        };
    }

    @Override
    public Session get() {
        Session session = sessionThreadLocal.get().peek();
        if (session == null) {
            throw new IllegalStateException("Attempt to use a JMS session without a transaction");
        }

        return session;
    }

    Session getCurrentTransaction() {
        return sessionThreadLocal.get().peek();
    }

    void push(Session session) {
        sessionThreadLocal.get().push(session);
    }

    Session pop() {
        return sessionThreadLocal.get().pop();
    }
}
