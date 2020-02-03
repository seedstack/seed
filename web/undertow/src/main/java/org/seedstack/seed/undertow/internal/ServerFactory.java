/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.undertow.internal;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.crypto.CryptoConfig;
import org.seedstack.seed.crypto.spi.SSLProvider;
import org.seedstack.seed.web.WebConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnio.Options;
import org.xnio.SslClientAuthMode;
import org.xnio.XnioWorker;

class ServerFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerFactory.class);
    private final XnioWorker xnioWorker;
    private final WebConfig.ServerConfig serverConfig;

    ServerFactory(XnioWorker xnioWorker, WebConfig.ServerConfig serverConfig) {
        this.xnioWorker = xnioWorker;
        this.serverConfig = serverConfig;
    }

    Undertow createServer(HttpHandler httpHandler, SSLProvider sslProvider) {
        PathHandler path = Handlers
                .path(Handlers.redirect(serverConfig.getContextPath()))
                .addPrefixPath(serverConfig.getContextPath(), httpHandler);

        Undertow.Builder builder = Undertow.builder().setWorker(xnioWorker);

        if (!serverConfig.isHttp() && !serverConfig.isHttps()) {
            throw SeedException.createNew(UndertowErrorCode.NO_LISTENER_CONFIGURED);
        } else {
            if (serverConfig.isHttp()) {
                builder = configureHttp(builder);
            }
            if (serverConfig.isHttps()) {
                builder = configureHttps(builder, sslProvider);
                if (serverConfig.isHttp2()) {
                    LOGGER.info("HTTP/2 support is enabled");
                    builder.setServerOption(UndertowOptions.ENABLE_HTTP2, serverConfig.isHttp2());
                }
            }
        }

        return builder.setHandler(path).build();
    }

    private Undertow.Builder configureHttp(Undertow.Builder builder) {
        LOGGER.info("Undertow listening for HTTP on {}:{}", serverConfig.getHost(), serverConfig.getPort());
        return builder.addHttpListener(serverConfig.getPort(), serverConfig.getHost());
    }

    private Undertow.Builder configureHttps(Undertow.Builder builder, SSLProvider sslProvider) {
        LOGGER.info("Undertow listening for HTTPS on {}:{}", serverConfig.getHost(), serverConfig.getSecurePort());
        CryptoConfig.SSLConfig sslConfig = sslProvider.sslConfig();
        return builder
                .addHttpsListener(serverConfig.getSecurePort(), serverConfig.getHost(), sslProvider.sslContext()
                        .orElseThrow(() -> SeedException.createNew(UndertowErrorCode.MISSING_SSL_CONTEXT)
                                .put("ksName", sslConfig.getKeystore())))
                .setSocketOption(Options.SSL_CLIENT_AUTH_MODE,
                        SslClientAuthMode.valueOf(sslConfig.getClientAuthMode().toString()));
    }
}
