/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.undertow;

import org.seedstack.coffig.Config;

/**
 * This class holds the properties used to configure undertow.
 */
@Config("web.server.undertow")
public class UndertowConfig {
    private static final int coreCount = Math.max(1, Runtime.getRuntime().availableProcessors());
    private static final boolean tinyMemory = Runtime.getRuntime().maxMemory() < 256 * 1024 * 1024;
    private int ioThreads = coreCount * 2;
    private int workerThreads = coreCount * 10;
    private boolean directBuffers = !tinyMemory;
    private int bufferSize = tinyMemory ? 1024 : 1024 * 16 - 20; // UNDERTOW-1209
    private boolean tcpNoDelay = true;
    private int readTimeout = 0;
    private int writeTimeout = 0;

    public int getBufferSize() {
        return bufferSize;
    }

    public UndertowConfig setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
        return this;
    }

    public int getIoThreads() {
        return ioThreads;
    }

    public UndertowConfig setIoThreads(int ioThreads) {
        this.ioThreads = ioThreads;
        return this;
    }

    public int getWorkerThreads() {
        return workerThreads;
    }

    public UndertowConfig setWorkerThreads(int workerThreads) {
        this.workerThreads = workerThreads;
        return this;
    }

    public boolean isDirectBuffers() {
        return directBuffers;
    }

    public UndertowConfig setDirectBuffers(boolean directBuffers) {
        this.directBuffers = directBuffers;
        return this;
    }

    public boolean isTcpNoDelay() {
        return tcpNoDelay;
    }

    public UndertowConfig setTcpNoDelay(boolean tcpNoDelay) {
        this.tcpNoDelay = tcpNoDelay;
        return this;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public UndertowConfig setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    public int getWriteTimeout() {
        return writeTimeout;
    }

    public UndertowConfig setWriteTimeout(int writeTimeout) {
        this.writeTimeout = writeTimeout;
        return this;
    }
}
