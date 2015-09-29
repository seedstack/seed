/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web.api;

import java.net.URL;

/**
 * This immutable class represent informations relative to a static web resource.
 *
 * @author adrien.lauer@mpsa.com
 */
public class ResourceInfo {
    final private URL url;
    final private boolean gzipped;
    final private String contentType;

    /**
     * Creates a resource information instance.
     *
     * @param url the resource contents URL.
     * @param gzipped if the resource can be gzipped.
     * @param contentType the content type of the resource.
     */
    public ResourceInfo(URL url, boolean gzipped, String contentType) {
        this.url = url;
        this.gzipped = gzipped;
        this.contentType = contentType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ResourceInfo that = (ResourceInfo) o;

        return gzipped == that.gzipped && contentType.equals(that.contentType) && url.equals(that.url);

    }

    @Override
    public int hashCode() {
        int result = url.hashCode();
        result = 31 * result + (gzipped ? 1 : 0);
        result = 31 * result + contentType.hashCode();
        return result;
    }

    /**
     * Get the resource contents URL.
     *
     * @return the URL.
     */
    public URL getUrl() {
        return url;
    }

    /**
     * Checks if the resource can be gzipped.
     *
     * @return true if it can be gzipped, false otherwise.
     */
    public boolean isGzipped() {
        return gzipped;
    }

    /**
     * Get the content type of the resource.
     *
     * @return the content type as a MIME type string.
     */
    public String getContentType() {
        return contentType;
    }
}