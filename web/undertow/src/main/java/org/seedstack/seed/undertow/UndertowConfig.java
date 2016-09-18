/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.undertow;

import org.seedstack.coffig.Config;

/**
 * This class holds the properties used to configure undertow.
 *
 * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
 */
@Config("web.server.undertow")
public class UndertowConfig {
    private Integer bufferSize;
    private Integer buffersPerRegion;
    private Integer ioThreads;
    private Integer workerThreads;
    private Boolean directBuffers;

    public Integer getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(Integer bufferSize) {
        this.bufferSize = bufferSize;
    }

    public Integer getBuffersPerRegion() {
        return buffersPerRegion;
    }

    public void setBuffersPerRegion(Integer buffersPerRegion) {
        this.buffersPerRegion = buffersPerRegion;
    }

    public Integer getIoThreads() {
        return ioThreads;
    }

    public void setIoThreads(Integer ioThreads) {
        this.ioThreads = ioThreads;
    }

    public Integer getWorkerThreads() {
        return workerThreads;
    }

    public void setWorkerThreads(Integer workerThreads) {
        this.workerThreads = workerThreads;
    }

    public Boolean getDirectBuffers() {
        return directBuffers;
    }

    public void setDirectBuffers(Boolean directBuffers) {
        this.directBuffers = directBuffers;
    }
}
