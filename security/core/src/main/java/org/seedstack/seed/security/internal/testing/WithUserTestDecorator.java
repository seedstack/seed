/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.internal.testing;

import java.util.Optional;
import javax.inject.Inject;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.seedstack.seed.security.WithUser;
import org.seedstack.seed.testing.spi.TestContext;
import org.seedstack.seed.testing.spi.TestDecorator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WithUserTestDecorator implements TestDecorator {
    private static final Logger LOGGER = LoggerFactory.getLogger(WithUserTestDecorator.class);
    @Inject
    private SecurityManager securityManager;
    private Subject subject;

    @Override
    public void beforeTest(TestContext testContext) {
        getWithUser(testContext).ifPresent(withUser -> {
            LOGGER.info("Logging user {} before executing test {}", withUser.id(), testContext.testName());
            ThreadContext.bind(securityManager);
            subject = new Subject.Builder(securityManager).buildSubject();
            subject.login(new UsernamePasswordToken(withUser.id(), withUser.password()));
            ThreadContext.bind(subject);
        });
    }

    @Override
    public void afterTest(TestContext testContext) {
        if (subject != null) {
            LOGGER.info("Logging user out", testContext.testMethod());
            subject.logout();
            ThreadContext.unbindSecurityManager();
            ThreadContext.unbindSubject();
        }
    }

    private Optional<WithUser> getWithUser(TestContext testContext) {
        return Optional.ofNullable(testContext.testMethod()
                .map(m -> m.getAnnotation(WithUser.class))
                .orElseGet(() -> testContext.testClass().getAnnotation(WithUser.class)));
    }
}
