/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web.internal;

import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.commons.configuration.Configuration;
import org.seedstack.seed.core.api.Application;
import org.seedstack.seed.core.api.SeedException;
import org.seedstack.seed.web.api.ResourceInfo;
import org.seedstack.seed.web.api.ResourceRequest;
import org.seedstack.seed.web.api.WebErrorCode;
import org.seedstack.seed.web.api.WebResourceResolver;

import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutionException;
import java.util.zip.GZIPOutputStream;

/**
 * This web resource filter serves static resource from the classpath and the docroot with some
 * benefits over the container default resource serving:
 * <p>
 * <ul>
 * <li>Multiples locations can be aggregated and served under the same path,</li>
 * <li>Automatic serving of pre-minified and/or pre-gzipped versions of resources,</li>
 * <li>On-the-fly gzipping of resources,</li>
 * <li>Cache friendly.</li>
 * </ul>
 *
 * @author adrien.lauer@mpsa.com
 */
class WebResourceFilter implements Filter {
    private static final int DEFAULT_CACHE_SIZE = 8192;
    private static final int DEFAULT_CACHE_CONCURRENCY = 32;
    private static final int DEFAULT_BUFFER_SIZE = 65536;
    private static final String HEADER_IFMODSINCE = "If-Modified-Since";
    private static final String HEADER_LASTMOD = "Last-Modified";

    private final LoadingCache<ResourceRequest, Optional<ResourceInfo>> resourceInfoCache;
    private final long servletInitTime;
    private final WebResourceResolver webResourceResolver;

    @Inject
    WebResourceFilter(final Application application, final WebResourceResolver webResourceResolver) {
        Configuration configuration = application.getConfiguration();

        this.servletInitTime = System.currentTimeMillis();

        this.webResourceResolver = webResourceResolver;

        int cacheSize = configuration.getInt(WebPlugin.WEB_PLUGIN_PREFIX + ".resources.cache.max-size", DEFAULT_CACHE_SIZE);
        this.resourceInfoCache = CacheBuilder.newBuilder().maximumSize(cacheSize).concurrencyLevel(configuration.getInt(WebPlugin.WEB_PLUGIN_PREFIX + ".resources.cache.concurrency", DEFAULT_CACHE_CONCURRENCY)).initialCapacity(configuration.getInt(WebPlugin.WEB_PLUGIN_PREFIX + ".resources.cache.initial-size", cacheSize / 4)).build(new CacheLoader<ResourceRequest, Optional<ResourceInfo>>() {
            @Override
            public Optional<ResourceInfo> load(ResourceRequest key) {
                ResourceInfo resourceInfo = webResourceResolver.resolveResourceInfo(key);
                if (resourceInfo == null) {
                    return Optional.absent();
                } else {
                    return Optional.of(resourceInfo);
                }
            }
        });
    }

    private ResourceData prepareResourceData(ResourceInfo resourceInfo, boolean acceptGzip) throws IOException {
        boolean gzippedOnTheFly = false;
        OutputStream os;
        ByteArrayOutputStream baos;
        if (acceptGzip && webResourceResolver.isCompressible(resourceInfo)) {
            baos = new ByteArrayOutputStream();
            os = new GZIPOutputStream(baos);
            gzippedOnTheFly = true;
        } else {
            os = baos = new ByteArrayOutputStream();
        }

        // Copy data
        InputStream is = null;
        try {
            is = resourceInfo.getUrl().openStream();
            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
            int readBytes = is.read(buffer);
            while (readBytes != -1) {
                os.write(buffer, 0, readBytes);
                readBytes = is.read(buffer);
            }
            os.close();
        } finally {
            if (is != null) {
                is.close();
            }
        }

        return new ResourceData(baos.toByteArray(), resourceInfo.isGzipped() || gzippedOnTheFly);
    }

    @Override
    public final void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        HttpServletRequest httpServletRequest;
        HttpServletResponse httpServletResponse;

        if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
            httpServletRequest = (HttpServletRequest) request;
            httpServletResponse = (HttpServletResponse) response;
        } else {
            throw new ServletException("WebResourceFilter can only serve HTTP request");
        }

        long ifModifiedSince;
        try {
            ifModifiedSince = httpServletRequest.getDateHeader(HEADER_IFMODSINCE);
        } catch (IllegalArgumentException iae) {
            // Invalid date header - proceed as if none was set
            ifModifiedSince = -1;
        }
        if (ifModifiedSince < (servletInitTime / 1000 * 1000)) {
            httpServletResponse.setDateHeader(HEADER_LASTMOD, servletInitTime);
        } else {
            httpServletResponse.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            return;
        }

        String acceptEncodingHeader = httpServletRequest.getHeader("Accept-Encoding");
        boolean acceptGzip = acceptEncodingHeader != null && acceptEncodingHeader.contains("gzip");

        // Find resource
        ResourceInfo resourceInfo = null;
        try {
            Optional<ResourceInfo> cached = resourceInfoCache.get(new ResourceRequest(getPathInfo(httpServletRequest), acceptGzip));
            if (cached.isPresent()) {
                resourceInfo = cached.get();
            }
        } catch (ExecutionException e) {
            throw SeedException.wrap(e, WebErrorCode.UNABLE_TO_DETERMINE_RESOURCE_INFO).put("path", getPathInfo(httpServletRequest));
        }

        if (resourceInfo != null) {
            // Prepare response
            response.setContentType(resourceInfo.getContentType());
            ResourceData resourceData = prepareResourceData(resourceInfo, acceptGzip);
            if (resourceData.gzipped) {
                httpServletResponse.addHeader("Content-Encoding", "gzip");
            }
            httpServletResponse.addHeader("Content-Length", Integer.toString(resourceData.data.length));

            // Write data
            response.getOutputStream().write(resourceData.data);
            return;
        }

        // Chain to next filter if no response was sent
        filterChain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // nothing to do
    }

    @Override
    public void destroy() {
        // nothing to do
    }

    private String getPathInfo(HttpServletRequest httpServletRequest) {
        return httpServletRequest.getRequestURI().substring(httpServletRequest.getContextPath().length());
    }

    private static class ResourceData {
        final byte[] data;
        final boolean gzipped;

        ResourceData(byte[] data, boolean gzipped) { //NOSONAR
            this.data = data;
            this.gzipped = gzipped;
        }
    }
}
