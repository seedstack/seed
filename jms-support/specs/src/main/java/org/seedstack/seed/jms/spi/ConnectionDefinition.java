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

public class ConnectionDefinition {
    private final boolean managed;
    private final int reconnectionDelay;
    private final boolean shouldSetClientId;
    private final String clientId;
    private final String user;
    private final String password;

    public ConnectionDefinition(boolean managed, boolean shouldSetClientId, String clientId, String user, String password, int reconnectionDelay) {
        this.managed = managed;
        this.reconnectionDelay = reconnectionDelay;
        this.shouldSetClientId = shouldSetClientId;
        this.clientId = clientId;
        this.user = user;
        this.password = password;
    }

    public boolean isManaged() {
        return managed;
    }

    public int getReconnectionDelay() {
        return reconnectionDelay;
    }

    public String getClientId() {
        return clientId;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public boolean isShouldSetClientId() {
        return shouldSetClientId;
    }
}
