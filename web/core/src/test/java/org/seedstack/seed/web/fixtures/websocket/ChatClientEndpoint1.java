/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web.fixtures.websocket;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.websocket.ClientEndpoint;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import org.seedstack.seed.web.websocket.BaseClientEndpointConfigurator;

@ClientEndpoint(configurator = BaseClientEndpointConfigurator.class)
public class ChatClientEndpoint1 {
    public static final String TEXT = "Client1 joins";
    private CountDownLatch latch = new CountDownLatch(1);
    private String response;

    @OnOpen
    public void onOpen(Session session) {
        try {
            session.getBasicRemote().sendText(TEXT);
        } catch (IOException ioe) {
            throw new IllegalStateException(ioe);
        }
    }

    @OnMessage
    public void processMessage(String message) {
        response = message;
        latch.countDown();
    }

    public void reset() {
        latch = new CountDownLatch(1);
        response = null;
    }

    public String awaitResponse() throws InterruptedException {
        if (latch.await(2, TimeUnit.SECONDS)) {
            return response;
        } else {
            throw new IllegalStateException("No response received");
        }
    }
}
