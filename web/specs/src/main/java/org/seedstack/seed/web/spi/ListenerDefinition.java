/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web.spi;

import java.util.EventListener;

public class ListenerDefinition {
    private final Class<? extends EventListener> listenerClass;
    private int priority = 0;

    public ListenerDefinition(Class<? extends EventListener> listenerClass) {
        this.listenerClass = listenerClass;
    }

    public Class<? extends EventListener> getListenerClass() {
        return listenerClass;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }
}
