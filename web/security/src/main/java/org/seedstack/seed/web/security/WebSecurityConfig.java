/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web.security;

import org.seedstack.coffig.Config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Config("security.web")
public class WebSecurityConfig {
    private List<UrlConfig> urls = new ArrayList<>();
    private XSRFConfig xsrf = new XSRFConfig();

    public List<UrlConfig> getUrls() {
        return Collections.unmodifiableList(urls);
    }

    public WebSecurityConfig addUrl(UrlConfig urlConfig) {
        urls.add(urlConfig);
        return this;
    }

    public XSRFConfig xsrf() {
        return xsrf;
    }

    public static class UrlConfig {
        private String pattern = "/**";
        private List<String> filters = new ArrayList<>();

        public String getPattern() {
            return pattern;
        }

        public UrlConfig setPattern(String pattern) {
            this.pattern = pattern;
            return this;
        }

        public List<String> getFilters() {
            return Collections.unmodifiableList(filters);
        }

        public UrlConfig addFilters(String... filters) {
            this.filters.addAll(Arrays.asList(filters));
            return this;
        }
    }

    @Config("xsrf")
    public static class XSRFConfig {
        private String cookieName = "XSRF-TOKEN";
        private String headerName = "X-XSRF-TOKEN";
        private String algorithm = "SHA1PRNG";
        private int length = 32;

        public String getCookieName() {
            return cookieName;
        }

        public XSRFConfig setCookieName(String cookieName) {
            this.cookieName = cookieName;
            return this;
        }

        public String getHeaderName() {
            return headerName;
        }

        public XSRFConfig setHeaderName(String headerName) {
            this.headerName = headerName;
            return this;
        }

        public String getAlgorithm() {
            return algorithm;
        }

        public XSRFConfig setAlgorithm(String algorithm) {
            this.algorithm = algorithm;
            return this;
        }

        public int getLength() {
            return length;
        }

        public XSRFConfig setLength(int length) {
            this.length = length;
            return this;
        }
    }
}
