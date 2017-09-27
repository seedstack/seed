/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.web.internal.resources;

import com.google.inject.assistedinject.Assisted;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.activation.MimetypesFileTypeMap;
import javax.inject.Inject;
import javax.servlet.ServletContext;
import org.seedstack.seed.Application;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.web.ResourceInfo;
import org.seedstack.seed.web.ResourceRequest;
import org.seedstack.seed.web.WebConfig;
import org.seedstack.seed.web.WebResourceResolver;
import org.seedstack.seed.web.internal.WebErrorCode;
import org.seedstack.shed.ClassLoaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class WebResourcesResolverImpl implements WebResourceResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebResourcesResolverImpl.class);
    private static final Pattern EXTENSION_PATTERN = Pattern.compile("\\.(\\w+)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern CONSECUTIVE_SLASHES_PATTERN = Pattern.compile("(/)\\1+");
    private static final String CLASSPATH_LOCATION = "META-INF/resources";
    private static final String MINIFIED_GZIPPED_EXT_PATTERN = ".min.$1.gz";
    private static final String GZIPPED_EXT_PATTERN = ".$1.gz";
    private static final String MINIFIED_EXT_PATTERN = ".min.$1";

    private final MimetypesFileTypeMap mimetypesFileTypeMap;

    private final boolean serveMinifiedResources;

    private final boolean serveGzippedResources;

    private final boolean onTheFlyGzipping;

    private final ClassLoader classLoader;

    private final ServletContext servletContext;

    @Inject
    WebResourcesResolverImpl(final Application application, @Assisted ServletContext servletContext) {
        WebConfig.StaticResourcesConfig staticResourcesConfig = application.getConfiguration().get(
                WebConfig.class).staticResources();
        this.servletContext = servletContext;
        this.classLoader = ClassLoaders.findMostCompleteClassLoader(WebResourcesResolverImpl.class);
        this.mimetypesFileTypeMap = new MimetypesFileTypeMap();
        this.serveMinifiedResources = staticResourcesConfig.isMinificationEnabled();
        this.serveGzippedResources = staticResourcesConfig.isGzipEnabled();
        this.onTheFlyGzipping = staticResourcesConfig.isOnTheFlyGzipEnabled();
    }

    @Override
    public ResourceInfo resolveResourceInfo(ResourceRequest resourceRequest) {
        String normalizedPath;

        if (resourceRequest.getPath() == null) {
            normalizedPath = "";
        } else {
            normalizedPath = CONSECUTIVE_SLASHES_PATTERN.matcher(resourceRequest.getPath()).replaceAll("$1");
        }

        if (normalizedPath.startsWith("/")) {
            normalizedPath = "" + normalizedPath;
        } else {
            normalizedPath = "/" + normalizedPath;
        }

        // Determine content type with the normalized path
        String contentType = mimetypesFileTypeMap.getContentType(normalizedPath);
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        Matcher matcher = EXTENSION_PATTERN.matcher(normalizedPath);
        URL resourceUrl;

        // search in docroot first (and META-INF/resources if servlet version is >= 3.0)
        try {
            if (resourceRequest.isAcceptGzip() && serveGzippedResources) {
                resourceUrl = this.servletContext.getResource(matcher.replaceAll(MINIFIED_GZIPPED_EXT_PATTERN));
                if (serveMinifiedResources && resourceUrl != null) {
                    return new ResourceInfo(resourceUrl, true, contentType);
                }

                resourceUrl = this.servletContext.getResource(matcher.replaceAll(GZIPPED_EXT_PATTERN));
                if (resourceUrl != null) {
                    return new ResourceInfo(resourceUrl, true, contentType);
                }
            }

            resourceUrl = this.servletContext.getResource(matcher.replaceAll(MINIFIED_EXT_PATTERN));
            if (serveMinifiedResources && resourceUrl != null) {
                return new ResourceInfo(resourceUrl, false, contentType);
            }

            resourceUrl = this.servletContext.getResource(normalizedPath);
            if (resourceUrl != null) {
                return new ResourceInfo(resourceUrl, false, contentType);
            }
        } catch (MalformedURLException e) {
            throw SeedException.wrap(e, WebErrorCode.ERROR_RETRIEVING_RESOURCE);
        }

        // search in classpath last
        if (resourceRequest.isAcceptGzip() && serveGzippedResources) {
            resourceUrl = classLoader.getResource(
                    CLASSPATH_LOCATION + matcher.replaceAll(MINIFIED_GZIPPED_EXT_PATTERN));
            if (serveMinifiedResources && resourceUrl != null) {
                return new ResourceInfo(resourceUrl, true, contentType);
            }

            resourceUrl = classLoader.getResource(CLASSPATH_LOCATION + matcher.replaceAll(GZIPPED_EXT_PATTERN));
            if (resourceUrl != null) {
                return new ResourceInfo(resourceUrl, true, contentType);
            }
        }

        resourceUrl = classLoader.getResource(CLASSPATH_LOCATION + matcher.replaceAll(MINIFIED_EXT_PATTERN));
        if (serveMinifiedResources && resourceUrl != null) {
            return new ResourceInfo(resourceUrl, false, contentType);
        }

        resourceUrl = classLoader.getResource(CLASSPATH_LOCATION + normalizedPath);
        if (resourceUrl != null) {
            return new ResourceInfo(resourceUrl, false, contentType);
        }

        return null;
    }

    @Override
    public URI resolveURI(String path) {
        String contextPath = this.servletContext.getContextPath();

        // Context path with a value of / is invalid per spec but may still be provided by server
        if ("/".equals(contextPath)) {
            contextPath = "";
        }

        if (path.startsWith(CLASSPATH_LOCATION)) {
            try {
                StringBuilder sb = new StringBuilder();

                if (!contextPath.isEmpty()) {
                    sb.append(contextPath);
                }

                sb.append(path.substring(CLASSPATH_LOCATION.length()));

                return new URI(null, sb.toString(), null);
            } catch (URISyntaxException e) {
                LOGGER.debug("Error during resolution of " + path, e);
                return null;
            }
        }

        return null;
    }

    @Override
    public boolean isCompressible(ResourceInfo resourceInfo) {
        return serveGzippedResources &&
                onTheFlyGzipping &&
                !resourceInfo.isGzipped() &&
                (resourceInfo.getContentType().startsWith("text/") || "application/json".equals(
                        resourceInfo.getContentType()));
    }
}
