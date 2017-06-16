/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web.security.internal.shiro;

import com.google.inject.Injector;
import org.apache.shiro.util.PatternMatcher;
import org.apache.shiro.web.filter.mgt.FilterChainResolver;
import org.apache.shiro.web.util.WebUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

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
        for (final Map.Entry<String, ShiroWebModule.FilterKey[]> entry : chains.entrySet()) {
            if (patternMatcher.matches(entry.getKey(), path)) {
                return new SimpleFilterChain(originalChain, Arrays.stream(entry.getValue())
                        .filter(Objects::nonNull)
                        .map(input -> injector.getInstance(input.getKey()))
                        .iterator()
                );
            }
        }
        return null;
    }

}
