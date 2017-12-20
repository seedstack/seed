/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
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

    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public int getIoThreads() {
        return ioThreads;
    }

    public void setIoThreads(int ioThreads) {
        this.ioThreads = ioThreads;
    }

    public int getWorkerThreads() {
        return workerThreads;
    }

    public void setWorkerThreads(int workerThreads) {
        this.workerThreads = workerThreads;
    }

    public boolean isDirectBuffers() {
        return directBuffers;
    }

    public void setDirectBuffers(boolean directBuffers) {
        this.directBuffers = directBuffers;
    }
}
