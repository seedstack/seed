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

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;

import javax.inject.Inject;
import javax.jms.Message;
import javax.jms.MessageListener;

class MessageListenerAdapter implements MessageListener {
    @Inject
    private static Injector injector;

    private final Key<MessageListener> key;
    private final String name;

    MessageListenerAdapter(String name) {
        this.key = Key.get(MessageListener.class, Names.named(name));
        this.name = name;
    }

    @Override
    public void onMessage(Message message) {
        injector.getInstance(key).onMessage(message);
    }

    @Override
    public String toString() {
        return name;
    }
}
