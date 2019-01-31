/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.init;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.List;
import java.util.regex.Pattern;

class SeedProxySelector extends ProxySelector {
    private final ProxySelector defaultProxySelector;
    private final List<Pattern> exclusions;
    private final Proxy httpProxy;
    private final Proxy httpsProxy;

    public SeedProxySelector(Proxy httpProxy, Proxy httpsProxy, ProxySelector defaultProxySelector,
            List<Pattern> exclusions) {
        this.httpProxy = httpProxy;
        this.httpsProxy = httpsProxy;
        this.defaultProxySelector = defaultProxySelector;
        this.exclusions = exclusions;
    }

    @Override
    public List<Proxy> select(URI uri) {
        if (uri == null) {
            throw new IllegalArgumentException("URI can't be null.");
        }
        String protocol = uri.getScheme();

        if (isNotExcluded(uri) && ("http".equalsIgnoreCase(protocol))) {
            return Lists.newArrayList(httpProxy);
        } else if (isNotExcluded(uri) && ("https".equalsIgnoreCase(protocol))) {
            return Lists.newArrayList(httpsProxy);
        } else if (defaultProxySelector != null) {
            return defaultProxySelector.select(uri);
        } else {
            return Lists.newArrayList(Proxy.NO_PROXY);
        }
    }

    @Override
    public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
        // nothing to do
    }

    private boolean isNotExcluded(URI uri) {
        String host = uri.getHost();
        if ("localhost".equals(host) || "127.0.0.1".equals(host)) {
            return false;
        }

        for (Pattern exclusion : exclusions) {
            if (exclusion.matcher(host).matches()) {
                return false;
            }
        }

        return true;
    }
}