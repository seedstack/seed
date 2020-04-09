/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.web.security.internal;

import java.security.cert.X509Certificate;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.seedstack.seed.security.X509CertificateToken;
import org.seedstack.seed.security.internal.realms.AuthenticationTokenWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A security filter that extracts the certificate from the request for later use
 */
public class X509CertificateFilter extends AuthenticatingFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(X509CertificateFilter.class);
    private static final String OPTIONAL = "optional";
    private boolean optional;

    @Override
    protected AuthenticationToken createToken(ServletRequest request, ServletResponse response) throws Exception {
        X509Certificate[] certificates = (X509Certificate[]) request.getAttribute(
                "javax.servlet.request.X509Certificate");
        if (certificates == null) {
            certificates = new X509Certificate[0];
        }
        return new AuthenticationTokenWrapper(new X509CertificateToken(certificates));
    }

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        return executeLogin(request, response);
    }

    protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException e, ServletRequest request,
            ServletResponse response) {
        if (optional) {
            return true;
        } else {
            WebSecurityPlugin.sendErrorToClient((HttpServletResponse) response,
                    LOGGER,
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "A valid certificate is required to gain access",
                    e);
            return false;
        }
    }

    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        if (mappedValue != null && ((String[]) mappedValue).length != 0) {
            optional = OPTIONAL.equals(((String[]) mappedValue)[0]);
        }
        Subject subject = getSubject(request, response);
        return subject.isAuthenticated();
    }
}
