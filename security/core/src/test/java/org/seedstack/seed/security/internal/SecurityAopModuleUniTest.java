/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.internal;

import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.mockito.internal.util.reflection.Whitebox;

import com.google.inject.Binder;

public class SecurityAopModuleUniTest {

	@Test
	public void testModule(){
		SecurityAopModule underTest = new SecurityAopModule();
		Binder b = mock(Binder.class);
		Whitebox.setInternalState(underTest, "binder", b);
		
		underTest.configure();
	}
}
