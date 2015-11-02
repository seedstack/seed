/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
/*
 * Creation : 27 févr. 2015
 */
package org.seedstack.seed.crypto;

import javax.xml.bind.DatatypeConverter;

/**
 * A Hash is the couple made of two byte arrays : one is the hashed string, the other being
 * the salt used to generate it. When comparing two hashes made with the same salt, it has to be also through the same algorithm
 * with the same parameters.
 *
 * @author yves.dautremay@mpsa.com
 */
public class Hash {

    private final byte[] hash;

    private final byte[] salt;

    /**
     * Constructor with byte arrays
     * @param hash the hashed string
     * @param salt the salt used
     */
    public Hash(byte[] hash, byte[] salt) {
        this.hash = hash;
        this.salt = salt;
    }

    /**
     * Constructor with strings
     * @param hash the hashed string
     * @param salt the used salt
     */
    public Hash(String hash, String salt) {
        this.hash = DatatypeConverter.parseHexBinary(hash);
        this.salt = DatatypeConverter.parseHexBinary(salt);
    }

    /**
     * Return the hash
     * @return a byte array being the hash
     */
    public byte[] getHash() {
        return hash;
    }

    /**
     * Returns the salt
     * @return a byte array being the salt
     */
    public byte[] getSalt() {
        return salt;
    }

    /**
     * Returns the hash
     * @return the hash as a string
     */
    public String getHashAsString() {
        return DatatypeConverter.printHexBinary(hash);
    }

    /**
     * Returns the salt
     * @return the salt as a String
     */
    public String getSaltAsString() {
        return DatatypeConverter.printHexBinary(salt);
    }

    /**
     * Gives the Hash formatted as hash:salt
     */
    public String toString() {
        return getHashAsString() + ":" + getSaltAsString();
    }
}
