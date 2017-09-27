/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.internal.transaction;

import org.aopalliance.intercept.MethodInvocation;
import org.seedstack.seed.transaction.Propagation;
import org.seedstack.seed.transaction.Transactional;

public class TransactionalMethods {
    @Transactional
    public void ok() {
    }

    @Transactional
    public void default_rollback(Throwable t) throws Throwable {
        throw t;
    }

    @Transactional(noRollbackFor = {IllegalStateException.class, IllegalArgumentException.class})
    public void no_rollback_for(Throwable t) throws Throwable {
        throw t;
    }

    @Transactional(rollbackOn = {IllegalArgumentException.class})
    public void rollback_for(Throwable t) throws Throwable {
        throw t;
    }

    @Transactional
    public void fail() {
        throw new RuntimeException();
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void required() {
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void mandatory() {
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void not_supported() {
    }

    @Transactional(propagation = Propagation.NESTED)
    public void nested() {
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public void supports() {
    }

    @Transactional(propagation = Propagation.NEVER)
    public void never() {
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void requires_new() {
    }

    public enum Enum {
        OK("ok"),
        FAIL("fail"),
        REQUIRED("required"),
        NOT_SUPPORTED("not_supported"),
        NESTED("nested"),
        SUPPORTS("supports"),
        NEVER("never"),
        REQUIRES_NEW("requires_new"),
        NO_ROLLBACK_FOR("no_rollback_for", Throwable.class),
        ROLLBACK_FOR("rollback_for", Throwable.class),
        DEFAULT_ROLLBACK("default_rollback", Throwable.class),
        MANDATORY("mandatory");

        private SimpleMethodInvocation simpleMethodInvocation;

        Enum(String methodName, Class<?>... parameterTypes) {
            try {
                this.simpleMethodInvocation = new SimpleMethodInvocation(new TransactionalMethods(),
                        TransactionalMethods.class.getMethod(methodName, parameterTypes), new Object[]{});
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException(e);
            }
        }

        public MethodInvocation getMethodInvocation() {
            return simpleMethodInvocation;
        }
    }
}
