/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security;

/**
 * Exception thrown during the authentication process when an
 * {@link AuthenticationToken} implementation is encountered that is not
 * supported by a {@link Realm}.
 */
public class UnsupportedTokenException extends AuthenticationException {

    /**
     * UID
     */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new UnsupportedTokenException.
     */
    public UnsupportedTokenException() {
        super();
    }

    /**
     * Constructs a new UnsupportedTokenException.
     *
     * @param message the reason for the exception
     */
    public UnsupportedTokenException(String message) {
        super(message);
    }

    /**
     * Constructs a new UnsupportedTokenException.
     *
     * @param cause the underlying Throwable that caused this exception to be thrown.
     */
    public UnsupportedTokenException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new UnsupportedTokenException.
     *
     * @param message the reason for the exception
     * @param cause   the underlying Throwable that caused this exception to be thrown.
     */
    public UnsupportedTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
