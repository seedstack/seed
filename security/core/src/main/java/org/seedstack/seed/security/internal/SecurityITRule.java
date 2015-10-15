/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.internal;

import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.seedstack.seed.it.api.ITBind;
import org.seedstack.seed.security.api.WithUser;

import javax.inject.Inject;

/**
 * MethodRule used to connect a user to seed security if annotation @ {@link org.seedstack.seed.security.api.WithUser}
 * is present on method or on target class.
 *
 * @author yves.dautremay@mpsa.com
 */
@ITBind
public class SecurityITRule implements TestRule {
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

                if (userAnno != null) {
                    ThreadContext.bind(securityManager);
                    Subject subject = new Subject.Builder(securityManager).buildSubject();
                    UsernamePasswordToken token = new UsernamePasswordToken(userAnno.id(), userAnno.password());
                    subject.login(token);
                    ThreadContext.bind(subject);
                }

                base.evaluate();
            }
        };
    }
}
