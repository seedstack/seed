/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web.security.internal.shiro;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.spi.Dependency;
import com.google.inject.spi.ProviderWithDependencies;
import org.apache.shiro.util.AntPathMatcher;
import org.apache.shiro.util.PatternMatcher;
import org.apache.shiro.web.filter.mgt.FilterChainResolver;

import java.util.Map;
import java.util.Set;

@Singleton
class FilterChainResolverProvider implements ProviderWithDependencies<FilterChainResolver> {
    @Inject
    Injector injector;


    private final Map<String, ShiroWebModule.FilterKey[]> chains;
    private final Set<Dependency<?>> dependencies;

    private PatternMatcher patternMatcher = new AntPathMatcher();

    public FilterChainResolverProvider(Map<String, ShiroWebModule.FilterKey[]> chains) {
        this.chains = chains;
        ImmutableSet.Builder<Dependency<?>> dependenciesBuilder = ImmutableSet.builder();
        for (String chain : chains.keySet()) {
            for (ShiroWebModule.FilterKey filterKey : chains.get(chain)) {
                dependenciesBuilder.add(Dependency.get(filterKey.getKey()));
            }
        }
        this.dependencies = dependenciesBuilder.build();
    }

    @Inject(optional = true)
    public void setPatternMatcher(PatternMatcher patternMatcher) {
        this.patternMatcher = patternMatcher;
    }

    public Set<Dependency<?>> getDependencies() {
        return dependencies;
    }

    public FilterChainResolver get() {
        return new SimpleFilterChainResolver(chains, injector, patternMatcher);
    }

}
