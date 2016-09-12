/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web.internal.security.shiro;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;
import com.google.inject.Injector;
import org.apache.shiro.util.PatternMatcher;
import org.apache.shiro.web.filter.mgt.FilterChainResolver;
import org.apache.shiro.web.util.WebUtils;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.util.Map;

class SimpleFilterChainResolver implements FilterChainResolver {
    private final Map<String, ShiroWebModule.FilterKey[]> chains;
    private final Injector injector;
    private final PatternMatcher patternMatcher;

    SimpleFilterChainResolver(Map<String, ShiroWebModule.FilterKey[]> chains, Injector injector, PatternMatcher patternMatcher) {
        this.chains = chains;
        this.injector = injector;
        this.patternMatcher = patternMatcher;
    }

    public FilterChain getChain(ServletRequest request, ServletResponse response, final FilterChain originalChain) {
        String path = WebUtils.getPathWithinApplication(WebUtils.toHttp(request));
        for (final String pathPattern : chains.keySet()) {
            if (patternMatcher.matches(pathPattern, path)) {
                return new SimpleFilterChain(originalChain, Iterators.transform(Iterators.forArray(chains.get(pathPattern)),
                        (Function<ShiroWebModule.FilterKey, Filter>) input -> injector.getInstance(input.getKey())));
            }
        }
        return null;
    }

}
