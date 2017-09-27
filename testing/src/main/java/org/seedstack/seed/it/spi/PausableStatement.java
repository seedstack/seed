/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.it.spi;

import java.util.List;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

/**
 * This JUnit statement allows to invoke a list of methods without directly evaluating the next statement in the chain.
 * Instead the next statement can be evaluated at a later time through the {@link #resume()} method.
 */
public class PausableStatement extends Statement {
    private final Statement next;
    private final Object target;
    private final List<FrameworkMethod> methods;

    /**
     * Creates a PausableStatement.
     *
     * @param next    the next statement to execute.
     * @param methods the methods to invoke before chaining to the next statement.
     * @param target  the test object.
     */
    public PausableStatement(Statement next, List<FrameworkMethod> methods, Object target) {
        this.next = next;
        this.methods = methods;
        this.target = target;
    }

    @Override
    public void evaluate() throws Throwable {
        for (FrameworkMethod method : methods) {
            method.invokeExplosively(target);
        }
        next.evaluate();
    }

    /**
     * Evaluate the registered methods without chaining to the next statement.
     *
     * @throws Throwable if something goes wrong.
     */
    public void evaluateAndPause() throws Throwable {
        for (FrameworkMethod method : methods) {
            method.invokeExplosively(target);
        }
    }

    /**
     * Resumes evaluation of the next statement in the chain.
     *
     * @throws Throwable if something goes wrong.
     */
    public void resume() throws Throwable {
        next.evaluate();
    }
}
