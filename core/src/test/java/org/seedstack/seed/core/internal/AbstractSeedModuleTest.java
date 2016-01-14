/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
/**
 * 
 */
package org.seedstack.seed.core.internal;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.seedstack.seed.core.internal.sample.TestModule;
import org.seedstack.seed.core.utils.sample.TestType;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;

/**
 * AbstractBaseModuleTest
 * 
 * @author redouane.loulou@ext.mpsa.com
 *
 */
public class AbstractSeedModuleTest {
	
	static class Holder {
		@Inject @Named("collectionQualifiedTestType") 
		TestType<Collection<String>, Collection<Collection<Long>>> collectionQualifiedTestType;

		@Inject @Named("collectionQualifiedTestType") 
		TestType<Collection<String>, String> collectionQualifiedTestType2;
		
		@Inject
		TestType<Collection<String>, Collection<Collection<Long>>> collectionTestType;
	}
	
	class HolderModule extends AbstractModule {

		@Override
		protected void configure() {
			bind(Holder.class);
		}		
	}

	@Test
	public void test() {
		Injector injector = Guice.createInjector(new TestModule(), new HolderModule());
		Holder holder = injector.getInstance(Holder.class);
		Assertions.assertThat(holder.collectionQualifiedTestType).isNotNull();
		Assertions.assertThat(holder.collectionQualifiedTestType2).isNotNull();
		Assertions.assertThat(holder.collectionTestType).isNotNull();
		
	}

}
