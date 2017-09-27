/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.it;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation can be used on integration tests to mark a Throwable class as being expected.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface Expect {

    /**
     * @return the expected class.
     */
    Class<? extends Throwable> value();

    /**
     * @return the testing step in which the exception is expected.
     */
    TestingStep step() default TestingStep.STARTUP;

    enum TestingStep {
        STARTUP,
        INSTANTIATION,
        SHUTDOWN
    }
}
