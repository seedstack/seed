/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web;

/**
 * This immutable class represent a request of information relative to a static Web resource.
 */
public class ResourceRequest {
    final private String path;
    final private boolean acceptGzip;

    /**
     * Creates a resource request which doesn't accept gzip.
     *
     * @param path the requested resource path.
     */
    public ResourceRequest(String path) {
        this.path = path;
        this.acceptGzip = false;
    }

    /**
     * Creates a resource request.
     *
     * @param path       the requested resource path.
     * @param acceptGzip if gzip is accepted.
     */
    public ResourceRequest(String path, boolean acceptGzip) {
        this.path = path;
        this.acceptGzip = acceptGzip;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ResourceRequest that = (ResourceRequest) o;

        return acceptGzip == that.acceptGzip && path.equals(that.path);

    }

    @Override
    public int hashCode() {
        int result = path.hashCode();
        result = 31 * result + (acceptGzip ? 1 : 0);
        return result;
    }

    public String getPath() {
        return path;
    }

    public boolean isAcceptGzip() {
        return acceptGzip;
    }
}
