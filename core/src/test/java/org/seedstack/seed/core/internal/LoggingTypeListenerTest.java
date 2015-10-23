/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal;

import org.seedstack.seed.core.api.Install;
import org.seedstack.seed.core.api.Logging;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.Fail;
import org.fest.reflect.core.Reflection;
import org.junit.Test;
import org.slf4j.Logger;

import java.lang.reflect.Field;

/**
 *
 * LoggingTypeListener Test
 *
 * @author redouane.loulou@ext.mpsa.com
 *
 */
public class LoggingTypeListenerTest {

	@Logging
	private static Logger logger;


	@Test
	public void annotationPresentTest() {
		try {
			LoggingTypeListener loggingTypeListener = new LoggingTypeListener();
			Boolean isAnnotated = Reflection.method("annotationPresent").withReturnType(Boolean.class).withParameterTypes(Field.class, Class.class)
					.in(loggingTypeListener).invoke(this.getClass().getDeclaredField("logger"), Logging.class);
			Assertions.assertThat(isAnnotated).isTrue();
		} catch (SecurityException e) {
			Fail.fail(e.getMessage());
		} catch (NoSuchFieldException e) {
			Fail.fail(e.getMessage());
		}
	}

	@Test
	public void annotationPresentTest2() {
		try {
			LoggingTypeListener loggingTypeListener = new LoggingTypeListener();
			Boolean isAnnotated = Reflection.method("annotationPresent").withReturnType(Boolean.class).withParameterTypes(Field.class, Class.class)
					.in(loggingTypeListener).invoke(this.getClass().getDeclaredField("logger"), Install.class);
			Assertions.assertThat(isAnnotated).isFalse();
		} catch (SecurityException e) {
			Fail.fail(e.getMessage());
		} catch (NoSuchFieldException e) {
			Fail.fail(e.getMessage());
		}
	}

}
