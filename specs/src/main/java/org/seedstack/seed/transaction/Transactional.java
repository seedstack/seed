/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.transaction;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Transaction method marker. If a method is marked with this annotation, transaction manager logic will be invoked
 * around the method.
 */
@Target({ElementType.METHOD, ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Transactional {
    /**
     * Specify the transaction propagation type needed for the marked element.
     *
     * @return the propagation type.
     */
    Propagation[] propagation() default {};

    /**
     * Define the transaction as having a read-only behavior. It is used as an hint to the transaction
     * manager to optimize its behavior. Note that it is not required for the transaction manager to enforce
     * strict read-only behavior.
     *
     * @return true if read-only false otherwise.
     */
    boolean[] readOnly() default {};

    /**
     * Define if a failure by a participating transaction should mark the surrounding transaction as rollback only.
     *
     * @return true if a failure as participation marks the whole transaction as rollback only, false otherwise.
     */
    boolean[] rollbackOnParticipationFailure() default {};

    /**
     * A list of exceptions to rollback on, if thrown by the transactional
     * method. These exceptions are propagated correctly after a rollback.
     *
     * @return the list of exception classes to rollback on.
     */
    Class<? extends Exception>[] rollbackOn() default {Exception.class};

    /**
     * A list of exceptions to <b>not</b> rollback on. A caveat to the rollbackOn
     * clause. The disjunction of rollbackOn and noRollbackFor represents the list of
     * exceptions that will trigger a rollback. The complement of rollbackOn and
     * the universal set plus any exceptions in the ignore set represents the
     * list of exceptions that will trigger a commit. Note that ignore
     * exceptions take precedence over rollbackOn, but with subtype granularity.
     *
     * @return the list of exception classes to NOT rollback for.
     */
    Class<? extends Exception>[] noRollbackFor() default {};
}
