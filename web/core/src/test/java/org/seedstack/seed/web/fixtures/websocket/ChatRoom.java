/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web.fixtures.websocket;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.websocket.Session;
import org.seedstack.seed.Bind;

@Bind
@Singleton
class ChatRoom {
    private final Set<Session> sessions = Collections.newSetFromMap(new ConcurrentHashMap<>());
    @Inject
    private EchoService echoService;

    void addClient(Session session) {
        sessions.add(session);
    }

    void removeClient(Session session) {
        sessions.remove(session);
    }

    void sendMessage(ChatMessage chatMessage) {
        for (Session session : sessions) {
            session.getAsyncRemote().sendText(echoService.echo(chatMessage.getMessage()));
        }
    }
}
