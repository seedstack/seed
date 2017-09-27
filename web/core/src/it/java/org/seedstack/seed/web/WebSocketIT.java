/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.seedstack.seed.it.AbstractSeedWebIT;
import org.seedstack.seed.web.fixtures.websocket.ChatClientEndpoint1;
import org.seedstack.seed.web.fixtures.websocket.ChatClientEndpoint2;

public class WebSocketIT extends AbstractSeedWebIT {
    @Inject
    ChatClientEndpoint1 chatClientEndpoint1;

    @Inject
    ChatClientEndpoint2 chatClientEndpoint2;

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class);
    }

    @Test
    @RunAsClient
    public void websocket_communication_is_working(@ArquillianResource URL baseUrl) throws Exception {
        ChatClientEndpoint1.latch = new CountDownLatch(1);

        final Session session1 = connectToServer(baseUrl, chatClientEndpoint1);
        assertThat(session1).isNotNull();

        assertThat(ChatClientEndpoint1.latch.await(2, TimeUnit.SECONDS)).isTrue();
        assertThat("echo: " + ChatClientEndpoint1.TEXT).isEqualTo(ChatClientEndpoint1.response);

        ChatClientEndpoint1.latch = new CountDownLatch(1);
        ChatClientEndpoint2.latch = new CountDownLatch(1);

        final Session session2 = connectToServer(baseUrl, chatClientEndpoint2);
        assertThat(session2).isNotNull();

        assertThat(ChatClientEndpoint1.latch.await(2,
                TimeUnit.SECONDS)).isTrue(); // FIXME there are concurrency issues here
        assertThat(ChatClientEndpoint2.latch.await(2, TimeUnit.SECONDS)).isTrue();
        assertThat("echo: " + ChatClientEndpoint2.TEXT).isEqualTo(ChatClientEndpoint1.response);
        assertThat("echo: " + ChatClientEndpoint2.TEXT).isEqualTo(ChatClientEndpoint2.response);
    }

    private Session connectToServer(URL baseUrl,
            Object endpoint) throws DeploymentException, IOException, URISyntaxException, IllegalAccessException,
            InstantiationException {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        URI uri = new URI("ws://" + baseUrl.getHost() + ":" + baseUrl.getPort() + baseUrl.getPath() + "chat");
        return container.connectToServer(endpoint, uri);
    }
}