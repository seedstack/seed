/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web.internal.security.shiro;

import com.google.inject.Key;
import org.apache.shiro.web.filter.PathMatchingFilter;

import java.util.Map;

class PathMatchingFilterProvider<T extends PathMatchingFilter> extends AbstractInjectionProvider<T> {
    private Map<String, String> pathConfigs;

    public PathMatchingFilterProvider(Key<T> key, Map<String, String> pathConfigs) {
        super(key);
        this.pathConfigs = pathConfigs;
    }

    @Override
    protected T postProcess(T filter) {
        for (Map.Entry<String, String> pathConfig : this.pathConfigs.entrySet()) {
            filter.processPathConfig(pathConfig.getKey(), pathConfig.getValue());
        }
        return filter;
    }
}
