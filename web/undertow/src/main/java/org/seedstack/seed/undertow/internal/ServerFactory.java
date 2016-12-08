/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.undertow.internal;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import io.undertow.server.handlers.PathHandler;
import io.undertow.servlet.api.DeploymentManager;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.crypto.spi.SSLProvider;
import org.seedstack.seed.undertow.UndertowConfig;
import org.seedstack.seed.web.WebConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnio.Options;
import org.xnio.SslClientAuthMode;

import javax.net.ssl.SSLContext;
import javax.servlet.ServletException;
import java.util.Optional;

class ServerFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerFactory.class);

    Undertow createServer(DeploymentManager manager, WebConfig.ServerConfig serverConfig, UndertowConfig undertowConfig, SSLProvider sslProvider) {
        PathHandler path = null;
        try {
            path = Handlers.path(Handlers.redirect(serverConfig.getContextPath()))
                    .addPrefixPath(serverConfig.getContextPath(), manager.start());
        } catch (ServletException e) {
            LOGGER.error(e.getMessage(), e);
        }

        Undertow.Builder builder;
        if (serverConfig.isHttps()) {
            builder = createHttpsBuilder(serverConfig, sslProvider);
        } else {
            builder = createHttpBuilder(serverConfig);
            builder.setServerOption(UndertowOptions.ENABLE_HTTP2, serverConfig.isHttp2());
        }

        return configureUndertow(builder, undertowConfig).setHandler(path).build();
    }

    private Undertow.Builder configureUndertow(Undertow.Builder builder, UndertowConfig undertowConfig) {
        undertowConfig.getBufferSize().ifPresent(builder::setBufferSize);
        undertowConfig.getBuffersPerRegion().ifPresent(builder::setBuffersPerRegion);
        undertowConfig.getIoThreads().ifPresent(builder::setIoThreads);
        undertowConfig.getWorkerThreads().ifPresent(builder::setWorkerThreads);
        undertowConfig.getDirectBuffers().ifPresent(builder::setDirectBuffers);
        return builder;
    }

    private Undertow.Builder createHttpBuilder(WebConfig.ServerConfig serverConfig) {
        return Undertow.builder().addHttpListener(serverConfig.getPort(), serverConfig.getHost());
    }

    private Undertow.Builder createHttpsBuilder(WebConfig.ServerConfig serverConfig, SSLProvider sslProvider) {
        Optional<SSLContext> sslContext = sslProvider.sslContext();
        if (sslContext.isPresent()) {
            return Undertow.builder()
                    .addHttpsListener(serverConfig.getPort(), serverConfig.getHost(), sslContext.get())
                    .setSocketOption(Options.SSL_CLIENT_AUTH_MODE, SslClientAuthMode.valueOf(sslProvider.sslConfig().getClientAuthMode().toString()));
        } else {
            throw SeedException.createNew(UndertowErrorCode.MISSING_SSL_CONFIGURATION);
        }
    }
}
