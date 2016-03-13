/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web.internal.resources;

import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.commons.configuration.Configuration;
import org.seedstack.seed.Application;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.web.ResourceInfo;
import org.seedstack.seed.web.ResourceRequest;
import org.seedstack.seed.web.WebResourceResolver;
import org.seedstack.seed.web.WebResourceResolverFactory;
import org.seedstack.seed.web.internal.WebErrorCode;
import org.seedstack.seed.web.internal.WebPlugin;

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
 * This web resource filter provides automatic static resource serving from the classpath and the docroot with some
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
    private final WebResourceResolverFactory webResourceResolverFactory;
    private WebResourceResolver webResourceResolver;

    @Inject
    WebResourceFilter(final Application application, final WebResourceResolverFactory webResourceResolverFactory) {
        Configuration configuration = application.getConfiguration();

        // round the time to nearest second for proper comparison with If-Modified-Since header
        this.servletInitTime = System.currentTimeMillis() / 1000L * 1000L;

        this.webResourceResolverFactory = webResourceResolverFactory;

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

    @Override
    public void init(FilterConfig config) throws ServletException {
        this.webResourceResolver = webResourceResolverFactory.createWebResourceResolver(config.getServletContext());
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
        String path = httpServletRequest.getRequestURI().substring(httpServletRequest.getContextPath().length());
        String acceptEncodingHeader = httpServletRequest.getHeader("Accept-Encoding");
        boolean acceptGzip = acceptEncodingHeader != null && acceptEncodingHeader.contains("gzip");

        if (path.isEmpty() || path.endsWith("/")) {
            filterChain.doFilter(servletRequest, servletResponse);
        } else {
            // Find resource
            ResourceInfo resourceInfo = null;
            try {
                Optional<ResourceInfo> cached = resourceInfoCache.get(new ResourceRequest(path, acceptGzip));
                if (cached.isPresent()) {
                    resourceInfo = cached.get();
                }
            } catch (ExecutionException e) {
                throw SeedException.wrap(e, WebErrorCode.UNABLE_TO_DETERMINE_RESOURCE_INFO).put("path", path);
            }

            if (resourceInfo == null) {
                filterChain.doFilter(servletRequest, servletResponse);
            } else {
                long ifModifiedSince = ((HttpServletRequest) servletRequest).getDateHeader(HEADER_IFMODSINCE);
                if (ifModifiedSince < servletInitTime) {
                    // Set last modified header
                    httpServletResponse.setDateHeader(HEADER_LASTMOD, servletInitTime);

                    // Prepare response
                    httpServletResponse.setContentType(resourceInfo.getContentType());
                    ResourceData resourceData = prepareResourceData(resourceInfo, acceptGzip);
                    if (resourceData.gzipped) {
                        httpServletResponse.addHeader("Content-Encoding", "gzip");
                    }
                    httpServletResponse.addHeader("Content-Length", Integer.toString(resourceData.data.length));

                    // Write data
                    httpServletResponse.getOutputStream().write(resourceData.data);
                } else {
                    // Send that resource was not modified
                    httpServletResponse.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                }
            }
        }
    }

    @Override
    public void destroy() {
        // nothing to do here
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

    private static class ResourceData {
        final byte[] data;
        final boolean gzipped;

        ResourceData(byte[] data, boolean gzipped) { //NOSONAR
            this.data = data;
            this.gzipped = gzipped;
        }
    }
}
