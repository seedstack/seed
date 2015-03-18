/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.cli.internal;

import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;

/**
 * @author epo.jemba@ext.mpsa.com
 */
class CommandLineModule extends AbstractModule {
	private static final Logger LOGGER = LoggerFactory.getLogger(CommandLineModule.class);
	private final Map<Class, Class> bindings;

	CommandLineModule(Map<Class,Class> bindings) {
		this.bindings = bindings;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected void configure() {
		for( Entry<Class, Class> binding : bindings.entrySet()  ) {
			LOGGER.info(String.format("Binding %s to %s.", binding.getKey(), binding.getValue()));
			bind(binding.getKey()).to(binding.getValue());
		}
	}

}
