/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.scan;

import org.reflections.vfs.Vfs;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Handle all url types not handled before during classpath scan to quietly log them. This should be the latest handler
 * registered.
 *
 * @author adrien.lauer@mpsa.com
 */
public class FallbackUrlType implements Vfs.UrlType {
    private List<URL> failedUrls = new ArrayList<>();

    @Override
    public boolean matches(URL url) {
        return true;
    }

    @Override
    public Vfs.Dir createDir(final URL url) {
        return new Vfs.Dir() {

            @Override
            public String getPath() {
                return url.getPath();
            }

            @Override
            public Iterable<Vfs.File> getFiles() {
                failedUrls.add(url);
                return new ArrayList<>();
            }

            @Override
            public void close() {
                // nothing to do
            }
        };
    }

    public List<URL> getFailedUrls() {
        return failedUrls;
    }
}
