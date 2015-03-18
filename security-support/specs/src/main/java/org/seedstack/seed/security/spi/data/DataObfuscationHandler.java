/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.spi.data;

/**
 * Interface to implement to obfuscate a data.
 *
 * @param <T> the type of data to obfuscate.
 * @author epo.jemba@ext.mpsa.com
 * @author pierre.thirouin@ext.mpsa.com
 */
public interface DataObfuscationHandler<T> {

	/**
	 * this methods will contains the logic to obfuscate your data given as input.
	 * <p>
	 * For instance an obfuscation rule on name "Dupont" could be "D."
	 * <p>
	 * 
	 * @param data the actual input to obfuscate.
	 * @return the data obfuscated.
	 */
	T obfuscate(T data);
	
}
