/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
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
