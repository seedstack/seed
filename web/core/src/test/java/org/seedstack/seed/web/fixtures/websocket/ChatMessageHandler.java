/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web.fixtures.websocket;

import javax.websocket.MessageHandler;
import javax.websocket.Session;

class ChatMessageHandler implements MessageHandler.Whole<String> {
    private final Session session;
    private final ChatRoom chatRoom;

    ChatMessageHandler(ChatRoom chatRoom, Session session) {
        this.chatRoom = chatRoom;
        this.session = session;
    }

    @Override
    public void onMessage(String message) {
        chatRoom.sendMessage(new ChatMessage(session, message));
    }
}
