/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.crypto;

import static java.util.Objects.requireNonNull;

import javax.xml.bind.DatatypeConverter;

/**
 * A Hash is the couple made of two byte arrays : the hashed string and the salt used to hash it. When comparing two
 * hashes made with the same salt, it has to be also through the same algorithm with the same parameters.
 */
public class Hash {
    private final byte[] hash;
    private final byte[] salt;

    /**
     * Constructor with byte arrays.
     *
     * @param hash the hashed string.
     * @param salt the salt used.
     */
    public Hash(byte[] hash, byte[] salt) {
        this.hash = requireNonNull(hash).clone();
        this.salt = requireNonNull(salt).clone();
    }

    /**
     * Constructor with strings.
     *
     * @param hash the hashed string.
     * @param salt the used salt.
     */
    public Hash(String hash, String salt) {
        this.hash = DatatypeConverter.parseHexBinary(requireNonNull(hash));
        this.salt = DatatypeConverter.parseHexBinary(requireNonNull(salt));
    }

    /**
     * Returns the hash.
     *
     * @return a byte array being the hash.
     */
    public byte[] getHash() {
        return hash.clone();
    }

    /**
     * Returns the salt.
     *
     * @return a byte array being the salt.
     */
    public byte[] getSalt() {
        return salt.clone();
    }

    /**
     * Returns the hash as a string.
     *
     * @return the hash as a string.
     */
    public String getHashAsString() {
        return DatatypeConverter.printHexBinary(hash);
    }

    /**
     * Returns the salt as a string.
     *
     * @return the salt as a string.
     */
    public String getSaltAsString() {
        return DatatypeConverter.printHexBinary(salt);
    }

    /**
     * Returns the Hash formatted as "hash:salt".
     */
    public String toString() {
        return getHashAsString() + ":" + getSaltAsString();
    }
}
