/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
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
import java.util.Optional;
import javax.net.ssl.SSLContext;
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

        if (serverConfig.isHttps()) {
            builder = configureHttps(builder, sslProvider);
        } else {
            builder = configureHttp(builder);
        }

        if (serverConfig.isHttp2()) {
            LOGGER.info("HTTP/2 support is enabled");
            builder.setServerOption(UndertowOptions.ENABLE_HTTP2, serverConfig.isHttp2());
        }

        return builder.setHandler(path).build();
    }

    private Undertow.Builder configureHttp(Undertow.Builder builder) {
        return builder.addHttpListener(serverConfig.getPort(), serverConfig.getHost());
    }

    private Undertow.Builder configureHttps(Undertow.Builder builder, SSLProvider sslProvider) {
        Optional<SSLContext> sslContext = sslProvider.sslContext();
        CryptoConfig.SSLConfig sslConfig = sslProvider.sslConfig();
        if (sslContext.isPresent()) {
            return builder
                    .addHttpsListener(serverConfig.getPort(), serverConfig.getHost(), sslContext.get())
                    .setSocketOption(Options.SSL_CLIENT_AUTH_MODE,
                            SslClientAuthMode.valueOf(sslConfig.getClientAuthMode().toString()));
        } else {
            throw SeedException.createNew(UndertowErrorCode.MISSING_SSL_CONTEXT)
                    .put("ksName", sslConfig.getKeyStore())
                    .put("tsName", sslConfig.getTrustStore())
                    .put("alias", sslConfig.getAlias());
        }
    }
}
