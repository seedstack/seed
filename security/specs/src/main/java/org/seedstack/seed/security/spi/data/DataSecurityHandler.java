/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.spi.data;

/**
 * Interface to implement in order to add behaviour around data security.
 * <p>
 * We use it to secure Fields, Methods or Contructors.
 *
 * @param <A> the type of the candidate.
 *  *  *          */
public interface DataSecurityHandler<A> {
	
	/**
	 * This methods helps to determine the security object out of the candidate.
	 * <p>
	 * most of the time it will be a string representing an expression language,
	 * but it can be anything.
	 * <p>
	 * @param candidate the candidate object that will be provided.
	 * @return the security expression
	 */
	Object securityExpression(A candidate);
	
	/**
	 * This methods helps to determine the {@link DataObfuscationHandler} out of the candidate.
	 * <p>
	 * 
	 * @param candidate the candidate object that will be provided.
	 * 
	 * @return the data obfuscation handler. the return can ben null.
	 */
	Class<? extends DataObfuscationHandler<?>> securityObfuscationHandler(A candidate);
	
}
