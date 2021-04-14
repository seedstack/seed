/*
 * Copyright © 2013-2021, The SeedStack authors <http://seedstack.org>
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
import io.undertow.server.handlers.builder.PredicatedHandler;
import io.undertow.server.handlers.builder.PredicatedHandlersParser;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.crypto.CryptoConfig;
import org.seedstack.seed.crypto.spi.SSLProvider;
import org.seedstack.seed.undertow.UndertowConfig;
import org.seedstack.seed.web.WebConfig;
import org.seedstack.shed.ClassLoaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnio.Options;
import org.xnio.SslClientAuthMode;
import org.xnio.XnioWorker;

import java.io.InputStream;
import java.util.List;

class ServerFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerFactory.class);
    private final XnioWorker xnioWorker;
    private final WebConfig.ServerConfig serverConfig;
    private final UndertowConfig undertowConfig;
    private final ClassLoader classLoader = ClassLoaders.findMostCompleteClassLoader(ServerFactory.class);

    ServerFactory(XnioWorker xnioWorker, WebConfig.ServerConfig serverConfig, UndertowConfig undertowConfig) {
        this.xnioWorker = xnioWorker;
        this.serverConfig = serverConfig;
        this.undertowConfig = undertowConfig;
    }

    Undertow createServer(HttpHandler httpHandler, SSLProvider sslProvider) {
        HttpHandler effectiveHttpHandler;

        // Configure context path if any
        if (!serverConfig.isRootContextPath()) {
            effectiveHttpHandler = Handlers
                    .path(Handlers.redirect(serverConfig.getContextPath()))
                    .addPrefixPath(serverConfig.getContextPath(), httpHandler);
        } else {
            effectiveHttpHandler = httpHandler;
        }

        // Configure handlers if any
        InputStream handlersFile = classLoader.getResourceAsStream(undertowConfig.getHandlersFile());
        if (handlersFile != null) {
            List<PredicatedHandler> handlers = PredicatedHandlersParser.parse(
                    handlersFile,
                    classLoader
            );
            effectiveHttpHandler = Handlers.predicates(handlers, effectiveHttpHandler);
        }


        // Configure HTTP(s) listeners
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

        // Build the server
        return builder
                .setHandler(effectiveHttpHandler)
                .build();
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
