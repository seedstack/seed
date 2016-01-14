/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.undertow.internal;

import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.servlet.api.DeploymentManager;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Tests the server factory which configure an Undertow server.
 *
 * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
 */
public class ServerFactoryTest {

    /**
     * Tests the undertow creation from a ServerConfig.
     */
    @Test
    public void testServerFactory() throws Exception {
        ServerConfig serverConfig = new ServerConfig();
        DeploymentManager manager = Mockito.mock(DeploymentManager.class);
        HttpHandler httpHandler = Mockito.mock(HttpHandler.class);
        Mockito.when(manager.start()).thenReturn(httpHandler);

        Undertow server = new ServerFactory().createServer(serverConfig, manager);

        Assertions.assertThat(server).isNotNull();
    }
}
