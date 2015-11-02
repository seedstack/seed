/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.internal;

import org.seedstack.seed.SeedException;
import org.seedstack.seed.rest.CacheControl;
import org.seedstack.seed.rest.CachePolicy;
import org.seedstack.seed.rest.ResourceFiltering;
import com.sun.jersey.api.model.AbstractMethod;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import com.sun.jersey.spi.container.ResourceFilter;
import com.sun.jersey.spi.container.ResourceFilterFactory;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@ResourceFiltering
class CacheFilterFactory implements ResourceFilterFactory {
    private static final List<ResourceFilter> NO_CACHE_FILTER = Collections.<ResourceFilter>singletonList(new CacheResponseFilter(CachePolicy.NO_CACHE));

    @Override
    public List<ResourceFilter> create(AbstractMethod am) {
        CacheControl cch = am.getAnnotation(CacheControl.class);
        if (cch == null) {
            cch = am.getClass().getAnnotation(CacheControl.class);
        }
        if (cch == null) {
            return NO_CACHE_FILTER;
        } else {
            return Collections.<ResourceFilter>singletonList(new CacheResponseFilter(cch.value()));
        }
    }

    private static class CacheResponseFilter implements ResourceFilter, ContainerResponseFilter {
        private final CachePolicy policy;

        CacheResponseFilter(CachePolicy policy) {
            this.policy = policy;
        }

        @Override
        public ContainerRequestFilter getRequestFilter() {
            return null;
        }

        @Override
        public ContainerResponseFilter getResponseFilter() {
            return this;
        }

        @Override
        public ContainerResponse filter(ContainerRequest request, ContainerResponse response) {
            switch (this.policy) {
                case NO_CACHE:
                    MultivaluedMap<String, Object> headers = response.getHttpHeaders();
                    headers.putSingle(HttpHeaders.LAST_MODIFIED, new Date());
                    headers.putSingle(HttpHeaders.EXPIRES, -1);
                    headers.putSingle(HttpHeaders.CACHE_CONTROL, "must revalidate, private");
                    break;
                case CUSTOM:
                    break;
                default:
                    throw SeedException.createNew(RestErrorCode.UNSUPPORTED_CACHE_POLICY).put("policy", this.policy.name());
            }
            return response;
        }
    }
}