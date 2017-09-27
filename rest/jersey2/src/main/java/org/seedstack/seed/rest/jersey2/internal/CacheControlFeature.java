/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.rest.jersey2.internal;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Date;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.rest.CacheControl;
import org.seedstack.seed.rest.CachePolicy;
import org.seedstack.seed.rest.internal.RestErrorCode;

@Provider
class CacheControlFeature implements DynamicFeature {
    private static final CacheResponseFilter NO_CACHE_FILTER = new CacheResponseFilter(CachePolicy.NO_CACHE);

    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext featureContext) {
        CacheControl cacheControl = null;
        Method resourceMethod = resourceInfo.getResourceMethod();
        if (resourceMethod != null) {
            cacheControl = resourceMethod.getAnnotation(CacheControl.class);
        } else {
            Class<?> resourceClass = resourceInfo.getResourceClass();
            if (resourceClass != null) {
                cacheControl = resourceClass.getAnnotation(CacheControl.class);
            }
        }

        if (cacheControl == null) {
            featureContext.register(NO_CACHE_FILTER);
        } else {
            featureContext.register(new CacheResponseFilter(cacheControl.value()));
        }
    }

    private static class CacheResponseFilter implements ContainerResponseFilter {
        private static final String MUST_REVALIDATE_PRIVATE = "must revalidate, private";
        private final CachePolicy policy;

        CacheResponseFilter(CachePolicy policy) {
            this.policy = policy;
        }

        @Override
        public void filter(ContainerRequestContext requestContext,
                ContainerResponseContext responseContext) throws IOException {
            switch (this.policy) {
                case NO_CACHE:
                    MultivaluedMap<String, Object> headers = responseContext.getHeaders();
                    headers.putSingle(HttpHeaders.LAST_MODIFIED, new Date());
                    headers.putSingle(HttpHeaders.EXPIRES, -1);
                    headers.putSingle(HttpHeaders.CACHE_CONTROL, MUST_REVALIDATE_PRIVATE);
                    break;
                case CUSTOM:
                    break;
                default:
                    throw SeedException.createNew(RestErrorCode.UNSUPPORTED_CACHE_POLICY)
                            .put("policy", this.policy.name());
            }
        }
    }
}
