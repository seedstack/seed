/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.crypto;

/**
 * Support for creating a hash and validating passwords.
 * When creating a hash from a string or char[], a random salt will be generated and
 * given with the hash through a {@link Hash} object.
 */
public interface HashingService {

    /**
     * Creates a hash from a string.
     *
     * @param toHash the String to hash.
     * @return a {@link Hash} giving the hash of the String and the Salt used.
     */
    Hash createHash(String toHash);

    /**
     * Creates a hash from a char[]. This method can be convenient as passwords are often represented in java as char[].
     *
     * @param toHash the char[] to hash.
     * @return a {@link Hash} giving the hash of the char[] and the Salt used.
     */
    Hash createHash(char[] toHash);

    /**
     * Validates a password using a hash.
     *
     * @param password    the password to check.
     * @param correctHash the hash of the valid password.
     * @return true if the password is correct, false if not.
     */
    boolean validatePassword(char[] password, Hash correctHash);

    /**
     * Validates a password using a hash.
     *
     * @param password    the password to check.
     * @param correctHash the hash of the valid password.
     * @return true if the password is correct, false if not.
     */
    boolean validatePassword(String password, Hash correctHash);
}