/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.web.spi;

import java.util.EventListener;

/**
 * This class holds the full definition of a Servlet listener. It can be returned as a collection element from
 * {@link WebProvider#listeners()}
 * to define the listeners that must be registered by Seed. The registered listener will be injectable and
 * interceptable.
 */
public class ListenerDefinition {
    private final Class<? extends EventListener> listenerClass;
    private int priority = SeedListenerPriority.NORMAL;

    /**
     * Creates a listener definition with the specified listener class.
     *
     * @param listenerClass the listener class.
     */
    public ListenerDefinition(Class<? extends EventListener> listenerClass) {
        this.listenerClass = listenerClass;
    }

    /**
     * @return the listener class.
     */
    public Class<? extends EventListener> getListenerClass() {
        return listenerClass;
    }

    /**
     * @return the registration priority of this listener ({@link SeedListenerPriority#NORMAL} by default).
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Sets the registration priority of this listener. Listeners are registered in the order of increasing priority.
     *
     * @param priority the absolute priority of this listener.
     */
    public void setPriority(int priority) {
        this.priority = priority;
    }
}
