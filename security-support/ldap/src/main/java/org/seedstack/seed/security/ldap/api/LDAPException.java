/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.ldap.api;

/**
 * Seed exception for LDAP errors.
 */
public class LDAPException extends Exception {

    private static final long serialVersionUID = -1042979744132023211L;

    /**
     * Constructor
     */
    public LDAPException() {
        super();
    }

    /**
     * Constructor
     * 
     * @param message message
     * @param cause cause
     */
    public LDAPException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor
     * 
     * @param message message
     */
    public LDAPException(String message) {
        super(message);
    }

    /**
     * Constructor
     * 
     * @param cause cause
     */
    public LDAPException(Throwable cause) {
        super(cause);
    }

}
