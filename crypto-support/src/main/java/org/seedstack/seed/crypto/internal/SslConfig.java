/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.crypto.internal;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * This class holds the SSL configuration information.
 *
 * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
 */
public class SslConfig {

    public static final List<String> CLIENT_AUTH_MODES = Lists.newArrayList("REQUIRED", "REQUESTED", "NOT_REQUESTED");

    private String protocol;
    private String clientAuthMode;
    private String[] ciphers;

    /**
     * @return the requested protocol
     */
    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    /**
     * @return the client authentication mode ("REQUIRED", "REQUESTED", "NOT_REQUESTED")
     */
    public String getClientAuthMode() {
        return clientAuthMode;
    }

    public void setClientAuthMode(String clientAuthMode) {
        this.clientAuthMode = clientAuthMode;
    }

    public String[] getCiphers() {
        return ciphers;
    }

    public void setCiphers(String[] ciphers) {
        this.ciphers = ciphers;
    }

}
