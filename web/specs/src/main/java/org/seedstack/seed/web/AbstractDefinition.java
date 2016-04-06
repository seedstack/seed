/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractDefinition {
    private final String name;
    private final Map<String, String> initParams = new HashMap<String, String>();
    private boolean asyncSupported = false;

    AbstractDefinition(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Map<String, String> getInitParameters() {
        return Collections.unmodifiableMap(initParams);
    }

    public void addInitParameters(Map<String, String> initParams) {
        this.initParams.putAll(initParams);
    }

    public boolean isAsyncSupported() {
        return asyncSupported;
    }

    public void setAsyncSupported(boolean asyncSupported) {
        this.asyncSupported = asyncSupported;
    }
}
