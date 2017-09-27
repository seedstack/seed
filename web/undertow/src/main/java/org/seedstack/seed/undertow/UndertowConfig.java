/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.undertow;

import java.util.Optional;
import org.seedstack.coffig.Config;

/**
 * This class holds the properties used to configure undertow.
 */
@Config("web.server.undertow")
public class UndertowConfig {
    private Optional<Integer> bufferSize = Optional.empty();
    private Optional<Integer> ioThreads = Optional.empty();
    private Optional<Integer> workerThreads = Optional.empty();
    private Optional<Boolean> directBuffers = Optional.empty();

    public Optional<Integer> getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(Integer bufferSize) {
        this.bufferSize = Optional.of(bufferSize);
    }

    public Optional<Integer> getIoThreads() {
        return ioThreads;
    }

    public void setIoThreads(Integer ioThreads) {
        this.ioThreads = Optional.of(ioThreads);
    }

    public Optional<Integer> getWorkerThreads() {
        return workerThreads;
    }

    public void setWorkerThreads(Integer workerThreads) {
        this.workerThreads = Optional.of(workerThreads);
    }

    public Optional<Boolean> getDirectBuffers() {
        return directBuffers;
    }

    public void setDirectBuffers(Boolean directBuffers) {
        this.directBuffers = Optional.of(directBuffers);
    }
}
