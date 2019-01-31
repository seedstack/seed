/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web;

import java.net.URL;

/**
 * This immutable class holds information relative to a static web resource.
 */
public class ResourceInfo {
    private final URL url;
    private final String externalForm;
    private final boolean gzipped;
    private final String contentType;

    /**
     * Creates a resource information instance.
     *
     * @param url         the resource contents URL.
     * @param gzipped     if the resource can be gzipped.
     * @param contentType the content type of the resource.
     */
    public ResourceInfo(URL url, boolean gzipped, String contentType) {
        this.url = url;
        this.externalForm = url.toExternalForm();
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

        return gzipped == that.gzipped && contentType.equals(that.contentType) && externalForm.equals(
                that.externalForm);

    }

    @Override
    public int hashCode() {
        int result = externalForm.hashCode();
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