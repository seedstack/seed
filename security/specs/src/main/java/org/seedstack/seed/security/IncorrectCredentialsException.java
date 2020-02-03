/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security;

/**
 * Exception to use when the credentials provided cannot be used to authentify
 * the subject.
 */
public class IncorrectCredentialsException extends AuthenticationException {

    /**
     * UID
     */
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
     * @param message the reason for the exception
     */
    public IncorrectCredentialsException(String message) {
        super(message);
    }

    /**
     * Constructs a new IncorrectCredentialsException.
     *
     * @param cause the underlying Throwable that caused this exception to be
     *              thrown.
     */
    public IncorrectCredentialsException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new IncorrectCredentialsException.
     *
     * @param message the reason for the exception
     * @param cause   the underlying Throwable that caused this exception to be
     *                thrown.
     */
    public IncorrectCredentialsException(String message, Throwable cause) {
        super(message, cause);
    }
}
