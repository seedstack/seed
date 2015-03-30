/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.unittest.helper;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.mockito.MockitoAnnotations;

/**
 * A rule to use annotations @Mock et @InjectMocks from
 * Mockito without using Mockito Runner.
 * 
 * @author yves.dautremay@mpsa.com
 * 
 */
public class MockitoRule implements TestRule {

	private final Object target;

    /**
     * Create the rule on the specified target.
     *
     * @param target the target to apply the rule to.
     */
	public MockitoRule(Object target) {
		this.target = target;
	}

	@Override
	public Statement apply(final Statement base, final Description description) {
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				MockitoAnnotations.initMocks(target);
				base.evaluate();
			}
		};
	}

}
