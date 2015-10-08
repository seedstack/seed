/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.api;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Capture a generic type and resolve it. For example:
 * 
 * <pre>
 * <code>
 *   new TypeOf&lt;Repository&lt;AggregateRoot&lt;Long&gt;,Long&gt;&gt;() {}
 * </code>
 * </pre>
 * 
 * @author thierry.bouvet@mpsa.com
 *
 * @param <T>
 *            Parameterized type to capture.
 */
public abstract class TypeOf<T> {

	private final Type type;
	private final Class<? super T> rawType;

	protected TypeOf() {
		Type superclass = getClass().getGenericSuperclass();
		if (!(superclass instanceof ParameterizedType)) {
			throw new SeedException(CoreErrorCode.MISSING_GENERIC_PARAMETER);
		}
		this.type = ((ParameterizedType) superclass).getActualTypeArguments()[0];
	    Class<?> clazz = type instanceof Class<?> ? (Class<?>)type : (Class<?>) ((ParameterizedType) type).getRawType();
	    @SuppressWarnings("unchecked")
		Class<? super T> clazz2 = (Class<? super T>) clazz;
		this.rawType = clazz2;

	}

	/**
	 * Returns the raw type with the generic types from this reference.
	 * 
	 * @return parameterized type
	 */
	public Type getType() {
		return this.type;
	}

	/**
	 * Returns the raw type from this reference.
	 * @return the rawType
	 */
	public Class<? super T> getRawType() {
		return rawType;
	}

}
