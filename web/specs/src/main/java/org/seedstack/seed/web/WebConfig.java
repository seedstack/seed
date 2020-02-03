/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import org.seedstack.coffig.Config;
import org.seedstack.coffig.SingleValue;
import org.seedstack.seed.validation.NotBlank;

@Config("web")
public class WebConfig {
    private boolean requestDiagnostic;
    private SessionTrackingMode sessionTrackingMode = SessionTrackingMode.COOKIE;
    private StaticResourcesConfig staticResources = new StaticResourcesConfig();
    private CORSConfig cors = new CORSConfig();
    private ServerConfig serverConfig = new ServerConfig();

    public boolean isRequestDiagnosticEnabled() {
        return requestDiagnostic;
    }

    public WebConfig setRequestDiagnostic(boolean requestDiagnostic) {
        this.requestDiagnostic = requestDiagnostic;
        return this;
    }

    public SessionTrackingMode getSessionTrackingMode() {
        return sessionTrackingMode;
    }

    public WebConfig setSessionTrackingMode(SessionTrackingMode sessionTrackingMode) {
        this.sessionTrackingMode = sessionTrackingMode;
        return this;
    }

    public StaticResourcesConfig staticResources() {
        return staticResources;
    }

    public CORSConfig cors() {
        return cors;
    }

    public ServerConfig serverConfig() {
        return serverConfig;
    }

    public enum SessionTrackingMode {
        COOKIE,
        SSL,
        URL
    }

    @Config("cors")
    public static class CORSConfig {
        @SingleValue
        private boolean enabled;
        private String path = "/*";
        private Map<String, String> properties = new HashMap<>();

        public boolean isEnabled() {
            return enabled;
        }

        public WebConfig.CORSConfig setEnabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public String getPath() {
            return path;
        }

        public WebConfig.CORSConfig setPath(String path) {
            this.path = path;
            return this;
        }

        public Map<String, String> getProperties() {
            return Collections.unmodifiableMap(properties);
        }

        public WebConfig.CORSConfig setProperties(Map<String, String> properties) {
            this.properties = new HashMap<>(properties);
            return this;
        }
    }

    @Config("static")
    public static class StaticResourcesConfig {
        private static final int DEFAULT_BUFFER_SIZE = 65535;

        @SingleValue
        private boolean enabled = true;
        private int bufferSize = DEFAULT_BUFFER_SIZE;
        private boolean minification = true;
        private boolean gzip = true;
        private boolean gzipOnTheFly = true;
        private CacheConfig cache = new CacheConfig();

        public boolean isEnabled() {
            return enabled;
        }

        public int getBufferSize() {
            return bufferSize;
        }

        public boolean isMinificationEnabled() {
            return minification;
        }

        public boolean isGzipEnabled() {
            return gzip;
        }

        public boolean isOnTheFlyGzipEnabled() {
            return gzipOnTheFly;
        }

        public CacheConfig cacheConfig() {
            return cache;
        }

        @Config("cache")
        public static class CacheConfig {
            private static final int DEFAULT_CACHE_MAX_SIZE = 8192;
            private static final int DEFAULT_CACHE_CONCURRENCY = 32;

            private int maxSize = DEFAULT_CACHE_MAX_SIZE;
            private int initialSize = maxSize / 4;
            private int concurrencyLevel = DEFAULT_CACHE_CONCURRENCY;

            public int getInitialSize() {
                return initialSize;
            }

            public int getMaxSize() {
                return maxSize;
            }

            public int getConcurrencyLevel() {
                return concurrencyLevel;
            }
        }
    }

    @Config("server")
    public static class ServerConfig {
        private static final String DEFAULT_HOST = "0.0.0.0";
        private static final int DEFAULT_PORT = 8080;
        private static final int DEFAULT_SECURE_PORT = 8443;
        private static final String DEFAULT_CONTEXT_PATH = "/";
        private static final boolean DEFAULT_HTTP_ACTIVATION = true;
        private static final boolean DEFAULT_HTTPS_ACTIVATION = false;
        private static final boolean DEFAULT_HTTP2_ACTIVATION = true;
        private static final String DEFAULT_WELCOME_FILE = "index.html";
        private static final boolean DEFAULT_PREFER_HTTPS = true;

        private SessionsConfig sessions = new SessionsConfig();
        private WebSocketConfig websocket = new WebSocketConfig();
        private String host = DEFAULT_HOST;
        @SingleValue
        @Min(0)
        @Max(65535)
        private int port = DEFAULT_PORT;
        @SingleValue
        @Min(0)
        @Max(65535)
        private int securePort = DEFAULT_SECURE_PORT;
        @NotBlank
        private String contextPath = DEFAULT_CONTEXT_PATH;
        private boolean http = DEFAULT_HTTP_ACTIVATION;
        private boolean https = DEFAULT_HTTPS_ACTIVATION;
        private boolean http2 = DEFAULT_HTTP2_ACTIVATION;
        private boolean preferHttps = DEFAULT_PREFER_HTTPS;
        private List<String> welcomeFiles = new ArrayList<>();
        private List<ErrorPage> errorPages = new ArrayList<>();

        public ServerConfig() {
            addWelcomeFile(DEFAULT_WELCOME_FILE);
        }

        public SessionsConfig sessions() {
            return sessions;
        }

        public WebSocketConfig webSocket() {
            return websocket;
        }

        public String getHost() {
            return host;
        }

        public ServerConfig setHost(String host) {
            this.host = host;
            return this;
        }

        public int getPort() {
            return port;
        }

        public ServerConfig setPort(int port) {
            this.port = port;
            return this;
        }

        public int getSecurePort() {
            return securePort;
        }

        public ServerConfig setSecurePort(int securePort) {
            this.securePort = securePort;
            return this;
        }

        public String getContextPath() {
            return contextPath;
        }

        public ServerConfig setContextPath(String contextPath) {
            String normalized = contextPath;
            if (!normalized.startsWith("/")) {
                normalized = "/" + normalized;
            }
            if (normalized.endsWith("/")) {
                normalized = normalized.substring(0, normalized.length() - 1);
            }
            this.contextPath = normalized;
            return this;
        }

        public boolean isHttp() {
            return http;
        }

        public ServerConfig setHttp(boolean http) {
            this.http = http;
            return this;
        }

        public boolean isHttps() {
            return https;
        }

        public ServerConfig setHttps(boolean https) {
            this.https = https;
            return this;
        }

        public boolean isHttp2() {
            return http2;
        }

        public ServerConfig setHttp2(boolean http2) {
            this.http2 = http2;
            return this;
        }

        public boolean isPreferHttps() {
            return preferHttps;
        }

        public ServerConfig setPreferHttps(boolean preferHttps) {
            this.preferHttps = preferHttps;
            return this;
        }

        public List<String> getWelcomeFiles() {
            return Collections.unmodifiableList(welcomeFiles);
        }

        public ServerConfig setWelcomeFiles(List<String> welcomeFiles) {
            this.welcomeFiles = new ArrayList<>(welcomeFiles);
            return this;
        }

        public ServerConfig addWelcomeFile(String welcomeFile) {
            this.welcomeFiles.add(welcomeFile);
            return this;
        }

        public List<ErrorPage> getErrorPages() {
            return Collections.unmodifiableList(errorPages);
        }

        public ServerConfig setErrorPages(List<ErrorPage> errorPages) {
            this.errorPages = errorPages;
            return this;
        }

        public ServerConfig addErrorPage(ErrorPage errorPage) {
            this.errorPages.add(errorPage);
            return this;
        }

        @Config("sessions")
        public static class SessionsConfig {
            private static final int DEFAULT_SESSION_TIMEOUT = 1000 * 60 * 15;
            @SingleValue
            private int timeout = DEFAULT_SESSION_TIMEOUT;

            public int getTimeout() {
                return timeout;
            }

            public SessionsConfig setTimeout(int timeout) {
                this.timeout = timeout;
                return this;
            }
        }

        @Config("websocket")
        public static class WebSocketConfig {
            @SingleValue
            private boolean enabled = true;

            public boolean isEnabled() {
                return enabled;
            }

            public WebSocketConfig setEnabled(boolean enabled) {
                this.enabled = enabled;
                return this;
            }
        }

        public static class ErrorPage {
            private String location;
            private Integer errorCode;
            private Class<? extends Exception> exceptionType;

            public String getLocation() {
                return location;
            }

            public ErrorPage setLocation(String location) {
                this.location = location;
                return this;
            }

            public Integer getErrorCode() {
                return errorCode;
            }

            public ErrorPage setErrorCode(Integer errorCode) {
                this.errorCode = errorCode;
                return this;
            }

            public Class<? extends Throwable> getExceptionType() {
                return exceptionType;
            }

            public ErrorPage setExceptionType(Class<? extends Exception> exceptionType) {
                this.exceptionType = exceptionType;
                return this;
            }

            public boolean isDefault() {
                return exceptionType == null && errorCode == null;
            }
        }
    }
}
