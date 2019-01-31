/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web.security;

import java.util.LinkedHashMap;
import java.util.Map;
import javax.servlet.Filter;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;

/**
 * This interface can be implemented in any authenticating Shiro filter to add subject session regeneration capability.
 * Typically this is done by overriding the {@code onLoggingSuccess()} method of such filter to call the
 * {@link #regenerateSession(Subject)} method before invoking normal behavior:
 *
 * <pre>
 * {@literal @}Override
 * protected boolean onLoginSuccess(AuthenticationToken token, Subject subject, ServletRequest request,
 *         ServletResponse response) throws Exception {
 *     regenerateSession(subject);
 *     return super.onLoginSuccess(token, subject, request, response);
 * }
 * </pre>
 */
public interface SessionRegeneratingFilter extends Filter {
    /**
     * Regenerate the session if any. This prevents a potential session fixation issue by forcing a new session id on
     * login success. See https://issues.apache.org/jira/browse/SHIRO-170.
     *
     * @param subject the successfully logged in subject
     */
    default void regenerateSession(Subject subject) {
        Session session = subject.getSession(false);
        if (session != null) {
            // Retain session attributes
            Map<Object, Object> attributes = new LinkedHashMap<>();
            for (Object key : session.getAttributeKeys()) {
                Object value = session.getAttribute(key);
                if (value != null) {
                    attributes.put(key, value);
                }
            }

            // Destroy the current sessions and recreate a new one
            session.stop();
            session = subject.getSession(true);

            // Restore attributes in the new session
            for (Map.Entry<Object, Object> entry : attributes.entrySet()) {
                session.setAttribute(entry.getKey(), entry.getValue());
            }
        }
    }
}
