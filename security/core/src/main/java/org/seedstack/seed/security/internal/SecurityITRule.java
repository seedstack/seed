/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.security.internal;

import javax.inject.Inject;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.seedstack.seed.it.ITBind;
import org.seedstack.seed.security.WithUser;

/**
 * MethodRule used to connect a user to seed security if annotation @ {@link WithUser}
 * is present on method or on target class.
 */
@ITBind
class SecurityITRule implements TestRule {
    /**
     * The securityManager to be used
     */
    @Inject
    private SecurityManager securityManager;

    @Override
    public Statement apply(final Statement base, final Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                WithUser userAnno = description.getAnnotation(WithUser.class);
                if (userAnno == null) {
                    userAnno = description.getTestClass().getAnnotation(WithUser.class);
                }

                Subject subject = null;
                if (userAnno != null) {
                    ThreadContext.bind(securityManager);
                    subject = new Subject.Builder(securityManager).buildSubject();
                    subject.login(new UsernamePasswordToken(userAnno.id(), userAnno.password()));
                    ThreadContext.bind(subject);
                }

                try {
                    base.evaluate();
                } finally {
                    if (subject != null) {
                        subject.logout();
                        ThreadContext.unbindSecurityManager();
                        ThreadContext.unbindSubject();
                    }
                }
            }
        };
    }
}
