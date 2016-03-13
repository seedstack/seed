/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web.internal.servlet;

import java.util.Map;

abstract class AbstractConfiguredElement {
    private String name;
    private String[] urlPatterns;
    private Map<String, String> initParams;

    String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    String[] getUrlPatterns() {
        return urlPatterns.clone();
    }

    void setUrlPatterns(String[] urlPatterns) {
        this.urlPatterns = urlPatterns.clone();
    }

    Map<String, String> getInitParams() {
        return initParams;
    }

    void setInitParams(Map<String, String> initParams) {
        this.initParams = initParams;
    }
}
