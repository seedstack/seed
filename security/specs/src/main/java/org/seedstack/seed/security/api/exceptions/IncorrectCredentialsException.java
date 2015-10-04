/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.api.exceptions;

/**
 * Exception to use when the credentials provided cannot be used to authentify
 * the subject.
 * 
 * @author yves.dautremay@mpsa.com
 * 
 */
public class IncorrectCredentialsException extends AuthenticationException {

    /** UID */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new IncorrectCredentialsException.
     */
    public IncorrectCredentialsException() {
        super();
    }

    /**
     * Constructs a new IncorrectCredentialsException.
     * 
     * @param message
     *            the reason for the exception
     */
    public IncorrectCredentialsException(String message) {
        super(message);
    }

    /**
     * Constructs a new IncorrectCredentialsException.
     * 
     * @param cause
     *            the underlying Throwable that caused this exception to be
     *            thrown.
     */
    public IncorrectCredentialsException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new IncorrectCredentialsException.
     * 
     * @param message
     *            the reason for the exception
     * @param cause
     *            the underlying Throwable that caused this exception to be
     *            thrown.
     */
    public IncorrectCredentialsException(String message, Throwable cause) {
        super(message, cause);
    }
}
