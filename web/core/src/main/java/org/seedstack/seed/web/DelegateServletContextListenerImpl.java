/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web;

import javax.servlet.ServletContextAttributeEvent;
import javax.servlet.ServletContextAttributeListener;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.List;


class DelegateServletContextListenerImpl implements ServletContextListener, ServletContextAttributeListener {
    private final List<ServletContextListener> listeners;
    private SeedServletContextListener.AbstractCallback callback;

    DelegateServletContextListenerImpl(List<ServletContextListener> listeners) {
        this.listeners = listeners;
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        for (ServletContextListener listener : listeners) {
            if (callback != null) {
                callback.onBeforeContextInitialized(listener);
            }
            listener.contextInitialized(sce);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        for (ServletContextListener listener : listeners) {
            listener.contextDestroyed(sce);
        }
    }

    public void setCallback(SeedServletContextListener.AbstractCallback callback) {
        this.callback = callback;
    }

    @Override
    public void attributeAdded(ServletContextAttributeEvent scab) {
        for (ServletContextListener listener : listeners) {
            if (listener instanceof ServletContextAttributeListener) {
                ((ServletContextAttributeListener) listener).attributeAdded(scab);
            }
        }
    }

    @Override
    public void attributeRemoved(ServletContextAttributeEvent scab) {
        for (ServletContextListener listener : listeners) {
            if (listener instanceof ServletContextAttributeListener) {
                ((ServletContextAttributeListener) listener).attributeRemoved(scab);
            }
        }
    }

    @Override
    public void attributeReplaced(ServletContextAttributeEvent scab) {
        for (ServletContextListener listener : listeners) {
            if (listener instanceof ServletContextAttributeListener) {
                ((ServletContextAttributeListener) listener).attributeReplaced(scab);
            }
        }
    }
}
