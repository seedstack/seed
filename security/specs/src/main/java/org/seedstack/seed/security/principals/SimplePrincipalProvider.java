/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.principals;

import java.io.Serializable;

/**
 * A named principal represented as a string.
 * 
 * @author yves.dautremay@mpsa.com
 * 
 */
public class SimplePrincipalProvider implements PrincipalProvider<String>, Serializable {

    /** UID */
    private static final long serialVersionUID = 3578609358630975912L;

    /** name */
	private String name;

	/** value */
	private String value;

	/**
	 * Constructor
	 * 
	 * @param name
	 *            name
	 * @param value
	 *            value
	 */
	public SimplePrincipalProvider(String name, String value) {
		this.name = name;
		this.value = value;
	}

	/**
	 * Getter name
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Getter value
	 * 
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	@Override
	public String getPrincipal() {
		return value;
	}
}
