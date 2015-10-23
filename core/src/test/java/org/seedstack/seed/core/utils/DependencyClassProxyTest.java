/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.utils;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.seedstack.seed.core.api.SeedException;

/**
 * Unit test for {@link DynamicClassProxy}.
 * @author thierry.bouvet@mpsa.com
 *
 */
public class DependencyClassProxyTest {

	public static abstract class AbstractDummyProxy {
	    protected abstract int getResult();
	};
	public abstract class AbstractDummyProxyError {
	};

	@Test
	public void testInvoke() {
		final int result = 10;
		AbstractDummyProxy proxy = new DependencyClassProxy<AbstractDummyProxy>(AbstractDummyProxy.class, new ProxyMethodReplacer() {
			@SuppressWarnings("unused")
			public int getResult(){
				return result;
			}
		}).getProxy();
		Assertions.assertThat(proxy.getResult()).isEqualTo(result);
	}
	
	@Test
	public void testInvokeWithException() {
		final String errorMessage = "dummy exception";
		AbstractDummyProxy proxy = new DependencyClassProxy<AbstractDummyProxy>(AbstractDummyProxy.class, new ProxyMethodReplacer() {
			@SuppressWarnings("unused")
			public int getResult(){
				throw new RuntimeException(errorMessage);
			}
		}).getProxy();
		try {
			proxy.getResult();
		} catch (Exception e) {
			Assertions.assertThat(e).isInstanceOf(RuntimeException.class);
			Assertions.assertThat(e.getMessage()).isEqualTo(errorMessage);
		}
	}

	@Test(expected=SeedException.class)
	public void testCreationError() {
		new DependencyClassProxy<AbstractDummyProxyError>(AbstractDummyProxyError.class, new ProxyMethodReplacer() {
		});
	}

}
