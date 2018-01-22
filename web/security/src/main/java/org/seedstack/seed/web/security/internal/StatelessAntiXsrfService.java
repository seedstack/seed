/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.web.security.internal;

import java.security.SecureRandom;
import javax.inject.Inject;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.web.security.WebSecurityConfig;
import org.seedstack.seed.web.spi.AntiXsrfService;

class StatelessAntiXsrfService implements AntiXsrfService {
    private final static char[] CHARSET = new char[]{'a', 'b', 'c', 'd', 'e',
            'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r',
            's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4',
            '5', '6', '7', '8', '9'};

    private final WebSecurityConfig.XSRFConfig xsrfConfig;

    @Inject
    public StatelessAntiXsrfService(WebSecurityConfig webSecurityConfig) {
        xsrfConfig = webSecurityConfig.xsrf();
    }

    @Override
    public void applyXsrfProtection(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        final String cookieName = xsrfConfig.getCookieName();
        final String headerName = xsrfConfig.getHeaderName();
        final HttpSession session = httpServletRequest.getSession(false);

        // Only apply XSRF protection when there is a session
        if (session != null) {
            // If session is new, generate a token and put it in a cookie
            if (session.isNew()) {
                Cookie cookie = new Cookie(cookieName, generateRandomToken());
                cookie.setHttpOnly(false);
                cookie.setPath("/");
                cookie.setMaxAge(-1);
                httpServletResponse.addCookie(cookie);
            }
            // Else, check if the request and cookie tokens match
            else {
                String cookieToken = extractCookieToken(cookieName, httpServletRequest);
                String requestToken = httpServletRequest.getHeader(headerName);

                if (requestToken == null) {
                    throw SeedException.createNew(WebSecurityErrorCode.MISSING_XSRF_HEADER);
                }

                if (cookieToken == null) {
                    throw SeedException.createNew(WebSecurityErrorCode.MISSING_XSRF_COOKIE);
                }

                // Check for multiple headers (keep only the first one)
                int commaIndex = requestToken.indexOf(',');
                if (commaIndex != -1) {
                    requestToken = requestToken.substring(0, commaIndex).trim();
                }

                // Check if tokens match
                if (!cookieToken.equals(requestToken)) {
                    throw SeedException.createNew(WebSecurityErrorCode.INVALID_XSRF_TOKEN);
                }
            }
        }
    }

    @Override
    public void cleanXsrfProtection(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        HttpSession session = httpServletRequest.getSession(false);

        // Delete an eventual existing token if no session
        if (session == null) {
            Cookie cookie = new Cookie(xsrfConfig.getCookieName(), "deleteMe");
            cookie.setHttpOnly(false);
            cookie.setPath("/");
            cookie.setMaxAge(0);
            httpServletResponse.addCookie(cookie);
        }
    }

    public String generateRandomToken() {
        final String tokenAlgorithm = xsrfConfig.getAlgorithm();
        final int tokenLength = xsrfConfig.getLength();

        try {
            SecureRandom secureRandom = SecureRandom.getInstance(tokenAlgorithm);
            StringBuilder sb = new StringBuilder();

            for (int i = 1; i < tokenLength + 1; i++) {
                sb.append(CHARSET[secureRandom.nextInt(CHARSET.length)]);

                if ((i % 4) == 0 && i != 0 && i < tokenLength) {
                    sb.append('-');
                }
            }

            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(
                    String.format("Unable to generate the random token - %s", e.getLocalizedMessage()), e);
        }
    }

    private String extractCookieToken(String cookieName, HttpServletRequest httpServletRequest) {
        for (Cookie cookie : httpServletRequest.getCookies()) {
            if (cookieName.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return null;
    }
}
