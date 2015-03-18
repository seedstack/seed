/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.internal.securityexpr;

import com.google.inject.AbstractModule;

/**
 *
 * 
 * @author epo.jemba@ext.mpsa.com
 * @author pierre.thirouin@ext.mpsa.com
 */
public class SecurityExpressionModule extends AbstractModule{
	
	@Override
	protected void configure() {
		bind(SecurityExpressionInterpreter.class);
		
		// we inject the security support
		requestStaticInjection(SecurityExpressionUtils.class);
		
		// TODO : add the SPI for Security Expression
	}

}
