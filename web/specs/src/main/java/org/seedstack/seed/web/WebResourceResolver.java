/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web;

import java.net.URI;

/**
 * This interface must be implemented by static Web resource resolvers. They can resolve {@link ResourceRequest}
 * in {@link ResourceInfo} and reverse resolve resource paths (i.e. a classpath resource)
 * in a corresponding URL.
 */
public interface WebResourceResolver {
    /**
     * Resolve a resource request (originating from HTTP) in a resource info object denoting the resource to serve.
     *
     * @param resourceRequest the request.
     * @return the {@link ResourceInfo} object denoting the resource to serve.
     */
    ResourceInfo resolveResourceInfo(ResourceRequest resourceRequest);

    /**
     * Reverse resolve a resource path (like a classpath resource path) into a Web-accessible URL if possible.
     *
     * @param resourcePath the resource path.
     * @return the Web-accessible URI if any, null otherwise.
     */
    URI resolveURI(String resourcePath);

    /**
     * Check if a resource can be compressed with gzip.
     *
     * @param resourceInfo the resource to check.
     * @return true if it can be compressed, false otherwise.
     */
    boolean isCompressible(ResourceInfo resourceInfo);
}
