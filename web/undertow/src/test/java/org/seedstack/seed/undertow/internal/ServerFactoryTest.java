/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.undertow.internal;

import static org.mockito.Mockito.when;

import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.servlet.api.DeploymentManager;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.mockito.Mockito;
import org.seedstack.seed.crypto.CryptoConfig;
import org.seedstack.seed.crypto.spi.SSLProvider;
import org.seedstack.seed.undertow.UndertowConfig;
import org.seedstack.seed.web.WebConfig;

/**
 * Tests the server factory which configure an Undertow server.
 */
public class ServerFactoryTest {

    /**
     * Tests the undertow creation from a ServerConfig.
     */
    @Test
    public void testServerFactory() throws Exception {
        WebConfig.ServerConfig serverConfig = new WebConfig.ServerConfig();
        UndertowConfig undertowConfig = new UndertowConfig();
        DeploymentManager manager = Mockito.mock(DeploymentManager.class);
        HttpHandler httpHandler = Mockito.mock(HttpHandler.class);
        SSLProvider sslProvider = Mockito.mock(SSLProvider.class);
        when(sslProvider.sslConfig()).thenReturn(new CryptoConfig.SSLConfig());
        when(sslProvider.sslContext()).thenReturn(Optional.empty());
        when(manager.start()).thenReturn(httpHandler);

        Undertow server = new ServerFactory(serverConfig, undertowConfig).createServer(manager, null);

        Assertions.assertThat(server).isNotNull();
    }
}
