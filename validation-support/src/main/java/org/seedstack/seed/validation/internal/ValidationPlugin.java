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

import org.seedstack.seed.validation.api.ValidationService;
import io.nuun.kernel.core.AbstractPlugin;

import javax.validation.Validation;
import javax.validation.ValidatorFactory;

/**
 * This plugin handle validation via jsr303 and jsr349 via hibernate-validation.
 * 
 * @author epo.jemba@ext.mpsa.com
 */
public class ValidationPlugin extends AbstractPlugin {
	private ValidationService validationService = new ValidationServiceInternal();
	private ValidationModule validationModule;
	private ValidatorFactory factory;
	
	@Override
	public String name() {
		return "seed-validation-plugin";
	}
	
	@Override
	public Object nativeUnitModule() {
		
		if (validationModule == null) {
			factory = Validation.buildDefaultValidatorFactory();
			
			validationModule = new ValidationModule(factory, validationService);
		}
		
		return validationModule;
	}

    /**
     * Get the validation service.
     *
     * @return the validation service.
     */
	public ValidationService getValidationService()	{
		return validationService;
	}
	
	@Override
	public void stop() {

		if (factory != null) {
			factory.close();
		}
		
	}
}
