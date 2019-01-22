/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.URL;
import javax.inject.Inject;
import javax.websocket.ContainerProvider;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.seedstack.seed.web.fixtures.websocket.ChatClientEndpoint1;
import org.seedstack.seed.web.fixtures.websocket.ChatClientEndpoint2;

@RunWith(Arquillian.class)
public class WebSocketIT {
    @ArquillianResource
    private URL baseUrl;
    @Inject
    private ChatClientEndpoint1 chatClientEndpoint1;
    @Inject
    private ChatClientEndpoint2 chatClientEndpoint2;

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class);
    }

    @Test
    @RunAsClient
    public void websocketCommunicationIsWorking() throws Exception {
        final Session session1 = openSession(chatClientEndpoint1);
        assertThat(session1).isNotNull();
        assertThat(chatClientEndpoint1.awaitResponse()).isEqualTo("echo: " + ChatClientEndpoint1.TEXT);

        chatClientEndpoint1.reset();

        final Session session2 = openSession(chatClientEndpoint2);
        assertThat(session2).isNotNull();
        assertThat(chatClientEndpoint1.awaitResponse()).isEqualTo("echo: " + ChatClientEndpoint2.TEXT);
        assertThat(chatClientEndpoint2.awaitResponse()).isEqualTo("echo: " + ChatClientEndpoint2.TEXT);

        session1.close();
        session2.close();
    }

    private Session openSession(Object endpoint) throws Exception {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        return container.connectToServer(endpoint, new URI(
                "ws://"
                        + baseUrl.getHost()
                        + ":"
                        + baseUrl.getPort()
                        + baseUrl.getPath()
                        + "chat"));
    }
}