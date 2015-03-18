/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.validation.internal;


import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;

import org.seedstack.seed.validation.api.ValidationService;
import org.seedstack.seed.validation.internal.test.BeanAll;
import org.seedstack.seed.validation.internal.test.BeanField;
import org.seedstack.seed.validation.internal.test.BeanMethodParam;
import org.seedstack.seed.validation.internal.test.BeanMethodReturnType;
import org.seedstack.seed.validation.internal.test.BeanNominal;

public class ValidationServiceTest {
	
	private ValidationService underTest;

	@Before
	public void init ()
	{
		underTest = new ValidationServiceInternal();
	}
	
	@Test
	public void validationservice_should_check_object_is_candidate_for_static_validation() {
		Assertions.assertThat ( underTest.candidateForStaticValidation ( BeanField.class)).isTrue();
		Assertions.assertThat ( underTest.candidateForStaticValidation ( BeanAll.class)).isTrue();
	}
	
	@Test
	public void validationservice_should_check_object_is_candidate_for_dynamic_validation() {
		Assertions.assertThat ( underTest.candidateForDynamicValidation ( BeanMethodParam.class)).isTrue();
		Assertions.assertThat ( underTest.candidateForDynamicValidation ( BeanMethodReturnType.class)).isTrue();
		Assertions.assertThat ( underTest.candidateForDynamicValidation ( BeanAll.class)).isTrue();
	}
	
	@Test
	public void validationservice_should_check_object_is_not_candidate_for_static_validation() {
		Assertions.assertThat ( underTest.candidateForStaticValidation ( BeanNominal.class)).isFalse();
	}
	
	@Test
	public void validationservice_should_check_object_is_not_candidate_for_dynamic_validation() {
		Assertions.assertThat ( underTest.candidateForDynamicValidation ( BeanNominal.class)).isFalse();
	}

}
