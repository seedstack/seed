/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.web.fixtures.websocket;

import javax.websocket.Session;

class ChatMessage {
    private final Session session;
    private final String message;

    ChatMessage(Session session, String message) {
        this.session = session;
        this.message = message;
    }

    public Session getSession() {
        return session;
    }

    public String getMessage() {
        return message;
    }
}
