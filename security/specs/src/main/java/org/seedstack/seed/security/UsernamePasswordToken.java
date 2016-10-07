/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security;


/**
 * The authentication token is based on a username/password couple.<br>
 * The host of the user is added if it exists.
 *
 * <p>Note that this class stores a password as a char[] instead of a String
 * (which may seem more logical).  This is because Strings are immutable and their
 * internal value cannot be overwritten - meaning even a nulled String instance might be accessible in memory at a later
 * time (e.g. memory dump).  This is not good for sensitive information such as passwords. For more information, see the
 * <a href="http://java.sun.com/j2se/1.5.0/docs/guide/security/jce/JCERefGuide.html#PBEEx">Java Cryptography Extension Reference Guide</a>.</p>
 *
 */
public class UsernamePasswordToken implements AuthenticationToken {

	/** UID */
	private static final long serialVersionUID = 1L;

	/** The username */
	private String username;

	/** The password, in char[] format */
	private char[] password;

	/**
	 * The location from where the login attempt occurs, or <code>null</code> if
	 * not known or explicitly omitted.
	 */
	private String host;

	/**
	 * Constructor
	 *
	 * @param username
	 *            username
	 * @param password
	 *            password
	 */
	public UsernamePasswordToken(String username, char[] password) {
		this(username, password, null);
	}

   /**
     * Constructor
     *
     * @param username
     *            username
     * @param password
     *            password
     */
    public UsernamePasswordToken(String username, String password) {
        this(username, password.toCharArray(), null);
    }

	/**
	 * Constructor
	 *
	 * @param username
	 *            username
	 * @param password
	 *            password
	 * @param host
	 *            host
	 */
	public UsernamePasswordToken(String username, char[] password, String host) {
		this.username = username;
        this.password = password.clone();
        this.host = host;
	}

	/**
	 * Getter username
	 *
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Getter password
	 *
	 * @return the password
	 */
	public char[] getPassword() {
		return password.clone();
	}

	/**
	 * Getter host
	 *
	 * @return the host
	 */
	public String getHost() {
		return host;
	}

	@Override
	public Object getPrincipal() {
		return username;
	}

	@Override
	public Object getCredentials() {
		return password;
	}

}
