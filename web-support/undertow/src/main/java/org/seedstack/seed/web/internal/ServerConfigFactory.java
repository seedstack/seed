/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web.internal;

import org.apache.commons.configuration.Configuration;
import org.seedstack.seed.crypto.internal.SslConfig;

import javax.net.ssl.SSLContext;

/**
 * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
 */
public class ServerConfigFactory {

    public ServerConfig create(Configuration configuration, SslConfig sslConfig, SSLContext sslContext) {
        ServerConfig serverConfig = new ServerConfig();
        if (configuration.containsKey("host")) {
            serverConfig.setHost(configuration.getString("host"));
        }
        if (configuration.containsKey("port")) {
            serverConfig.setPort(configuration.getInt("port"));
        }
        if (configuration.containsKey("context-path")) {
            serverConfig.setContextPath(configuration.getString("context-path"));
        }
        if (configuration.containsKey("https-enabled")) {
            serverConfig.setHttpsEnabled(configuration.getBoolean("https-enabled"));
        }
        if (configuration.containsKey("http2-enabled")) {
            serverConfig.setHttp2Enabled(configuration.getBoolean("http2-enabled"));
        }

        // Undertow configuration

        if (configuration.containsKey("buffer-size")) {
            serverConfig.setBufferSize(configuration.getInt("buffer-size"));
        }
        if (configuration.containsKey("buffers-per-region")) {
            serverConfig.setBuffersPerRegion(configuration.getInt("buffers-per-region"));
        }
        if (configuration.containsKey("io-threads")) {
            serverConfig.setIoThreads(configuration.getInt("io-threads"));
        }
        if (configuration.containsKey("worker-threads")) {
            serverConfig.setWorkerThreads(configuration.getInt("worker-threads"));
        }
        if (configuration.containsKey("direct-buffers")) {
            serverConfig.setDirectBuffers(configuration.getBoolean("direct-buffers"));
        }

        serverConfig.setSslConfig(sslConfig);
        serverConfig.setSslContext(sslContext);
        return serverConfig;
    }
}
