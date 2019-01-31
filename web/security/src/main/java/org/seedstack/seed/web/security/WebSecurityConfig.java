/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web.security;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.validation.constraints.NotBlank;
import org.seedstack.coffig.Config;

@Config("security.web")
public class WebSecurityConfig {
    private List<UrlConfig> urls = new ArrayList<>();
    private XSRFConfig xsrf = new XSRFConfig();
    private FormConfig form = new FormConfig();
    private String loginUrl = "/login.html";
    private String logoutUrl = "/";
    private String successUrl = "/";

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

    public FormConfig form() {
        return form;
    }

    public String getSuccessUrl() {
        return successUrl;
    }

    public WebSecurityConfig setSuccessUrl(String successUrl) {
        this.successUrl = successUrl;
        return this;
    }

    public String getLoginUrl() {
        return loginUrl;
    }

    public WebSecurityConfig setLoginUrl(String loginUrl) {
        this.loginUrl = loginUrl;
        return this;
    }

    public String getLogoutUrl() {
        return logoutUrl;
    }

    public WebSecurityConfig setLogoutUrl(String logoutUrl) {
        this.logoutUrl = logoutUrl;
        return this;
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

    @Config("form")
    public static class FormConfig {
        private String usernameParameter = "username";
        private String passwordParameter = "password";
        private String rememberMeParameter = "rememberMe";
        private String failureAttribute = "shiroLoginFailure";

        public String getUsernameParameter() {
            return usernameParameter;
        }

        public FormConfig setUsernameParameter(String usernameParameter) {
            this.usernameParameter = usernameParameter;
            return this;
        }

        public String getPasswordParameter() {
            return passwordParameter;
        }

        public FormConfig setPasswordParameter(String passwordParameter) {
            this.passwordParameter = passwordParameter;
            return this;
        }

        public String getRememberMeParameter() {
            return rememberMeParameter;
        }

        public FormConfig setRememberMeParameter(String rememberMeParameter) {
            this.rememberMeParameter = rememberMeParameter;
            return this;
        }

        public String getFailureAttribute() {
            return failureAttribute;
        }

        public FormConfig setFailureAttribute(String failureAttribute) {
            this.failureAttribute = failureAttribute;
            return this;
        }
    }

    @Config("xsrf")
    public static class XSRFConfig {
        @NotBlank
        private String cookieName = "XSRF-TOKEN";
        private String cookieSameSite = "Strict";
        private boolean cookieHttpOnly = false;
        private String cookiePath = "/";
        @NotBlank
        private String headerName = "X-XSRF-TOKEN";
        @NotBlank
        private String paramName = "xsrfToken";
        @NotBlank
        private String algorithm = "SHA1PRNG";
        private int length = 32;
        private boolean perRequestToken = false;
        private List<String> ignoreHttpMethods = Lists.newArrayList("GET", "HEAD", "OPTIONS");

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

        public boolean isPerRequestToken() {
            return perRequestToken;
        }

        public XSRFConfig setPerRequestToken(boolean perRequestToken) {
            this.perRequestToken = perRequestToken;
            return this;
        }

        public String getCookiePath() {
            return cookiePath;
        }

        public XSRFConfig setCookiePath(String cookiePath) {
            this.cookiePath = cookiePath;
            return this;
        }

        public String getCookieSameSite() {
            return cookieSameSite;
        }

        public XSRFConfig setCookieSameSite(String cookieSameSite) {
            this.cookieSameSite = cookieSameSite;
            return this;
        }

        public boolean isCookieHttpOnly() {
            return cookieHttpOnly;
        }

        public XSRFConfig setCookieHttpOnly(boolean cookieHttpOnly) {
            this.cookieHttpOnly = cookieHttpOnly;
            return this;
        }

        public String getParamName() {
            return paramName;
        }

        public XSRFConfig setParamName(String paramName) {
            this.paramName = paramName;
            return this;
        }

        public List<String> getIgnoreHttpMethods() {
            return ignoreHttpMethods;
        }

        public XSRFConfig setIgnoreHttpMethods(List<String> ignoreHttpMethods) {
            this.ignoreHttpMethods = ignoreHttpMethods;
            return this;
        }
    }
}
