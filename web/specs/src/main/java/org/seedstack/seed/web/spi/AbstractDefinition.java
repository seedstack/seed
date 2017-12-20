/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.web.spi;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This class holds common attributes for {@link FilterDefinition} and {@link ServletDefinition}.
 */
public abstract class AbstractDefinition {
    private final String name;
    private final Map<String, String> initParams = new HashMap<>();
    private boolean asyncSupported = false;

    AbstractDefinition(String name) {
        this.name = name;
    }

    /**
     * @return the name of the filter or servlet.
     */
    public String getName() {
        return name;
    }

    /**
     * @return the init parameters of the filter or servlet.
     */
    public Map<String, String> getInitParameters() {
        return Collections.unmodifiableMap(initParams);
    }

    /**
     * Add multiple init parameters to the filter or servlet definition.
     *
     * @param initParams the init parameters to add.
     */
    public void addInitParameters(Map<String, String> initParams) {
        this.initParams.putAll(initParams);
    }

    /**
     * @return true if asynchronous request are supported by this filter or servlet.
     */
    public boolean isAsyncSupported() {
        return asyncSupported;
    }

    /**
     * Sets if asynchronous requests are supported by this filter or servlet.
     *
     * @param asyncSupported true if asynchronous requests are supported, false otherwise.
     */
    public void setAsyncSupported(boolean asyncSupported) {
        this.asyncSupported = asyncSupported;
    }
}
