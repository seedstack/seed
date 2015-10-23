/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.seedstack.seed.it.AbstractSeedWebIT;

import javax.inject.Inject;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class WebSocketIT extends AbstractSeedWebIT {
    @Inject
    ChatClientEndpoint1 chatClientEndpoint1;

    @Inject
    ChatClientEndpoint2 chatClientEndpoint2;

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class).setWebXML("WEB-INF/web.xml");
    }

    @Test
    @RunAsClient
    public void websocket_communication_is_working(@ArquillianResource URL baseUrl) throws Exception {
        ChatClientEndpoint1.latch = new CountDownLatch(1);

        final Session session1 = connectToServer(baseUrl, chatClientEndpoint1);
        assertNotNull(session1);

        assertTrue(ChatClientEndpoint1.latch.await(2, TimeUnit.SECONDS));
        assertEquals("echo: " + ChatClientEndpoint1.TEXT, ChatClientEndpoint1.response);

        ChatClientEndpoint1.latch = new CountDownLatch(1);
        ChatClientEndpoint2.latch = new CountDownLatch(1);

        final Session session2 = connectToServer(baseUrl, chatClientEndpoint2);
        assertNotNull(session2);

        assertTrue(ChatClientEndpoint1.latch.await(2, TimeUnit.SECONDS));
        assertTrue(ChatClientEndpoint2.latch.await(2, TimeUnit.SECONDS));
        assertEquals("echo: " + ChatClientEndpoint2.TEXT, ChatClientEndpoint1.response);
        assertEquals("echo: " + ChatClientEndpoint2.TEXT, ChatClientEndpoint2.response);
    }

    private Session connectToServer(URL baseUrl, Object endpoint) throws DeploymentException, IOException, URISyntaxException, IllegalAccessException, InstantiationException {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        URI uri = new URI("ws://" + baseUrl.getHost() + ":" + baseUrl.getPort() + baseUrl.getPath() + "chat");
        return container.connectToServer(endpoint, uri);
    }
}