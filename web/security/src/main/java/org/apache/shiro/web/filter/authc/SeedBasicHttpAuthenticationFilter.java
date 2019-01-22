/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.apache.shiro.web.filter.authc;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.Subject;
import org.seedstack.seed.web.security.SessionRegeneratingFilter;

/**
 * This override of {@link BasicHttpAuthenticationFilter} ensures that the subject session is regenerated on login
 * success, avoiding potential session fixation vulnerability. It is used by default when SeedStack web security module
 * is in use.
 */
public class SeedBasicHttpAuthenticationFilter extends BasicHttpAuthenticationFilter
        implements SessionRegeneratingFilter {
    @Override
    protected boolean onLoginSuccess(AuthenticationToken token, Subject subject, ServletRequest request,
            ServletResponse response) throws Exception {
        regenerateSession(subject);
        return super.onLoginSuccess(token, subject, request, response);
    }
}
