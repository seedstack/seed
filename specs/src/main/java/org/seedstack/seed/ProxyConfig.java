/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed;

import javax.validation.constraints.NotNull;
import org.seedstack.coffig.Config;
import org.seedstack.coffig.SingleValue;

@Config("proxy")
public class ProxyConfig {
    @NotNull
    private ProxyMode mode = ProxyMode.AUTO;
    @SingleValue
    private String httpProxy;
    private String httpsProxy;
    private String noProxy;

    public ProxyMode getMode() {
        return mode;
    }

    public ProxyConfig setMode(ProxyMode mode) {
        this.mode = mode;
        return this;
    }

    public String getHttpProxy() {
        return httpProxy;
    }

    public ProxyConfig setHttpProxy(String httpProxy) {
        this.httpProxy = httpProxy;
        return this;
    }

    public String getHttpsProxy() {
        return httpsProxy == null ? httpProxy : httpsProxy;
    }

    public ProxyConfig setHttpsProxy(String httpsProxy) {
        this.httpsProxy = httpsProxy;
        return this;
    }

    public String getNoProxy() {
        return noProxy;
    }

    public ProxyConfig setNoProxy(String noProxy) {
        this.noProxy = noProxy;
        return this;
    }

    public enum ProxyMode {
        DISABLED,
        ENABLED,
        AUTO
    }
}
