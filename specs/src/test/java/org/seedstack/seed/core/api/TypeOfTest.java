/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
/**
 * 
 */
package org.seedstack.seed.core.api;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Test;

/**
 * Unit test for {@link TypeOf}
 * @author thierry.bouvet@mpsa.com
 *
 */
public class TypeOfTest {

	/**
	 * Test method for {@link org.seedstack.seed.core.api.TypeOf#getType()}.
	 */
	@Test
	public void testGetType() {
		TypeOf<List<String>> typeOf = new TypeOf<List<String>>() {
		};
		Assertions.assertThat(typeOf.getType().toString()).isEqualTo("java.util.List<java.lang.String>");
		
	}

	/**
	 * Test method for {@link org.seedstack.seed.core.api.TypeOf#getType()}.
	 * Test a {@link SeedException} if no generic parameter.
	 */
	@SuppressWarnings("rawtypes")
	@Test
	public void testGetTypeWithoutParameterized() {
        StringWriter stringWriter = new StringWriter();
		try {
			new TypeOf() {};
			Assertions.fail("Should throw a SeedException");
		} catch (SeedException e) {
			e.printStackTrace(new PrintWriter(stringWriter));
	        String text = stringWriter.toString();
	        Assertions.assertThat(text).contains("Missing generic parameter");
	        Assertions.assertThat(text).contains("Check that class has generic parameter");
		}
	}

}
