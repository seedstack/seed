/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web.security.internal;

import java.security.SecureRandom;
import java.util.List;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.shiro.web.filter.PathMatchingFilter;
import org.seedstack.seed.Configuration;
import org.seedstack.seed.web.security.WebSecurityConfig;

public class AntiXsrfFilter extends PathMatchingFilter {
    private final static char[] CHARSET = new char[]{'a', 'b', 'c', 'd', 'e',
            'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r',
            's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4',
            '5', '6', '7', '8', '9'};
    private static final String SET_COOKIE_HEADER = "Set-Cookie";
    private static final String NO_CHECK = "noCheck";
    @Configuration
    private WebSecurityConfig.XSRFConfig xsrfConfig;

    @Override
    protected boolean onPreHandle(ServletRequest request, ServletResponse response,
            Object mappedValue) throws Exception {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        final HttpSession session = httpServletRequest.getSession(false);

        // Only apply XSRF protection when there is a session
        if (session != null) {
            // If session is new, generate a token and put it in a cookie
            if (session.isNew()) {
                setXsrfCookie(httpServletResponse);
            }
            // Else, apply XSRF protection logic
            else {
                final boolean noCheck;
                if (mappedValue != null && ((String[]) mappedValue).length != 0) {
                    noCheck = NO_CHECK.equals(((String[]) mappedValue)[0]);
                } else {
                    noCheck = false;
                }

                if (!noCheck && !isRequestIgnored(httpServletRequest)) {
                    String cookieToken = getTokenFromCookie(httpServletRequest);

                    // If no cookie is available, send an error
                    if (cookieToken == null) {
                        ((HttpServletResponse) response).sendError(HttpServletResponse.SC_FORBIDDEN,
                                "Missing CSRF protection token cookie");
                        return false;
                    }

                    // Try to obtain the request token from a header
                    String requestToken = getTokenFromHeader(httpServletRequest);

                    // Fallback to query parameter if we didn't a token in the headers
                    if (requestToken == null) {
                        requestToken = getTokenFromParameter(httpServletRequest);
                    }

                    // If no request token available, send an error
                    if (requestToken == null) {
                        ((HttpServletResponse) response).sendError(HttpServletResponse.SC_FORBIDDEN,
                                "Missing CSRF protection token in the request headers");
                        return false;
                    }

                    // If tokens don't match, send an error
                    if (!cookieToken.equals(requestToken)) {
                        ((HttpServletResponse) response).sendError(HttpServletResponse.SC_FORBIDDEN,
                                "Request token does not match session token");
                        return false;
                    }

                    // Regenerate token if per-request tokens are in use
                    if (xsrfConfig.isPerRequestToken()) {
                        setXsrfCookie(httpServletResponse);
                    }
                }
            }
        }
        return true;
    }

    protected boolean isRequestIgnored(HttpServletRequest httpServletRequest) {
        List<String> ignoreHttpMethods = xsrfConfig.getIgnoreHttpMethods();
        return ignoreHttpMethods != null && ignoreHttpMethods.contains(httpServletRequest.getMethod());
    }

    protected String getTokenFromParameter(HttpServletRequest httpServletRequest) {
        return httpServletRequest.getParameter(xsrfConfig.getParamName());
    }

    protected String getTokenFromHeader(HttpServletRequest httpServletRequest) {
        String header = httpServletRequest.getHeader(xsrfConfig.getHeaderName());
        if (header != null) {
            int commaIndex = header.indexOf(',');
            if (commaIndex != -1) {
                // If header is multi-valued, only keep the first one
                header = header.substring(0, commaIndex).trim();
            }
        }
        return header;
    }

    protected void postHandle(ServletRequest request, ServletResponse response) {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        final HttpSession session = httpServletRequest.getSession(false);

        // Delete an eventual existing token if no session
        if (session == null) {
            deleteXsrfCookie(httpServletResponse);
        }
    }

    protected void setXsrfCookie(HttpServletResponse httpServletResponse) {
        String cookieSpec = String.format("%s=%s; Path=%s; SameSite=%s",
                xsrfConfig.getCookieName(),
                generateRandomToken(),
                xsrfConfig.getCookiePath(),
                xsrfConfig.getCookieSameSite());
        if (xsrfConfig.isCookieHttpOnly()) {
            cookieSpec = String.format("%s; HttpOnly", cookieSpec);
        }
        httpServletResponse.setHeader(SET_COOKIE_HEADER, cookieSpec);
    }

    protected void deleteXsrfCookie(HttpServletResponse httpServletResponse) {
        String cookieSpec = String.format("%s=%s; Max-Age=0",
                xsrfConfig.getCookieName(),
                "deleteMe");
        httpServletResponse.setHeader(SET_COOKIE_HEADER, cookieSpec);
    }

    protected String getTokenFromCookie(HttpServletRequest httpServletRequest) {
        String cookieName = xsrfConfig.getCookieName();
        for (Cookie cookie : httpServletRequest.getCookies()) {
            if (cookieName.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    protected String generateRandomToken() {
        final String tokenAlgorithm = xsrfConfig.getAlgorithm();
        final int tokenLength = xsrfConfig.getLength();

        try {
            SecureRandom secureRandom = SecureRandom.getInstance(tokenAlgorithm);
            StringBuilder sb = new StringBuilder();

            for (int i = 1; i < tokenLength + 1; i++) {
                sb.append(CHARSET[secureRandom.nextInt(CHARSET.length)]);

                if (i % 4 == 0 && i < tokenLength) {
                    sb.append('-');
                }
            }

            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(
                    String.format("Unable to generate the random token - %s", e.getLocalizedMessage()), e);
        }
    }
}
