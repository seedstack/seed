/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.internal.data;

import static org.assertj.core.api.Assertions.assertThat;

import javax.inject.Inject;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.collect.Lists;
import org.seedstack.seed.it.SeedITRunner;
import org.seedstack.seed.security.api.WithUser;
import org.seedstack.seed.security.api.data.DataSecurityService;
import org.seedstack.seed.security.internal.data.sample.MyUnsecuredPojo;
import org.seedstack.seed.security.internal.data.sample.SuperSecretPojo;

/**
 *
 * 
 * @author epo.jemba@ext.mpsa.com
 *
 */
@RunWith(SeedITRunner.class)
public class DataSecurityServiceInternalIT {
	
	@Inject
	DataSecurityService dataSecurityService;

	@Test
	public void testSecure() {
		Assertions.assertThat(dataSecurityService).isNotNull();
		Assertions.assertThat(dataSecurityService.getClass()).isNotNull();
	}
	@Test
	@WithUser(id = "Anakin", password = "imsodark"   )
	public void testNullValues() {
		SuperSecretPojo superPojo = new SuperSecretPojo();
		
		dataSecurityService.secure(superPojo);
		
		assertThat(superPojo. getMyint()).isEqualTo(0);
		assertThat(superPojo. getMyInteger()).isNull();
		assertThat(superPojo. getMyshort() ).isEqualTo((short)0);
		assertThat(superPojo. getMyShort() ).isNull();
		assertThat(superPojo. isMyboolean() ).isFalse();
		assertThat(superPojo. getMyBoolean() ).isNull();
		assertThat(superPojo. getMybyte() ).isEqualTo((byte)0);
		assertThat(superPojo. getMyByte() ).isNull();
		assertThat(superPojo. getMylong() ).isEqualTo(0l);
		assertThat(superPojo. getMyLong() ).isNull();
		assertThat(superPojo. getMyfloat() ).isEqualTo(0f);
		assertThat(superPojo. getMyFloat() ).isNull();
		assertThat(superPojo. getMydouble() ).isEqualTo(0d);
		assertThat(superPojo. getMyDouble() ).isNull();
		assertThat(superPojo. getMycharacter() ).isEqualTo((char)0);
		assertThat(superPojo. getMyCharacter() ).isNull();
		assertThat(superPojo. getMyString() ).isNull();
		
		
	}
	
	@Test
    @WithUser(id = "Anakin", password = "imsodark"   )
	public void test() {
		MyUnsecuredPojo pojo = new MyUnsecuredPojo("Doe", "john", "password", 12345);
		MyUnsecuredPojo pojo2 = new MyUnsecuredPojo("Doe", "jane", "password", 12345);
		
		dataSecurityService.secure(Lists.newArrayList(pojo, pojo2));

		assertThat(pojo.getName()).isEqualTo("D.");
		assertThat(pojo.getSalary()).isEqualTo(12000);
		assertThat(pojo.getPassword()).isNull();
	}

}
