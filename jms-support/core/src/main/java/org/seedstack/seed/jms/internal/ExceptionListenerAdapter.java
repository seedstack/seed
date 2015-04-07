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
import javax.jms.ExceptionListener;
import javax.jms.JMSException;

class ExceptionListenerAdapter implements ExceptionListener {
    @Inject
    private static Injector injector;

    private final Key<ExceptionListener> key;
    private final String name;

    ExceptionListenerAdapter(String name) {
        this.key = Key.get(ExceptionListener.class, Names.named(name));
        this.name = name;
    }

    @Override
    public void onException(JMSException exception) {
        injector.getInstance(key).onException(exception);
    }

    @Override
    public String toString() {
        return name;
    }
}
