/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web.fixtures.websocket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Inject;
import javax.websocket.MessageHandler;
import javax.websocket.OnClose;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import org.seedstack.seed.web.websocket.BaseServerEndpointConfigurator;

@ServerEndpoint(value = "/chat", configurator = BaseServerEndpointConfigurator.class)
public class ChatEndpoint {
    private final Map<Session, MessageHandler> messageHandlers = new ConcurrentHashMap<>();
    @Inject
    private ChatRoom chatRoom;

    @OnOpen
    public void onOpen(Session session) {
        ChatMessageHandler handler = new ChatMessageHandler(chatRoom, session);
        if (messageHandlers.put(session, handler) == null) {
            chatRoom.addClient(session);
            session.addMessageHandler(handler);
        }
    }

    @OnClose
    public void onClose(Session session) {
        MessageHandler handler = messageHandlers.get(session);
        if (handler != null) {
            session.removeMessageHandler(handler);
            chatRoom.removeClient(session);
        }
    }
}