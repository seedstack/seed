/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.crypto;

import static java.util.Objects.requireNonNull;

/**
 * A Hash is the couple made of two byte arrays : the hashed string and the salt used to hash it. When comparing two
 * hashes made with the same salt, it has to be also through the same algorithm with the same parameters.
 */
public class Hash {
    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
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
        this.hash = hexToBytes(requireNonNull(hash));
        this.salt = hexToBytes(requireNonNull(salt));
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
        return bytesToHex(hash);
    }

    /**
     * Returns the salt as a string.
     *
     * @return the salt as a string.
     */
    public String getSaltAsString() {
        return bytesToHex(salt);
    }

    /**
     * Returns the Hash formatted as "hash:salt".
     */
    public String toString() {
        return getHashAsString() + ":" + getSaltAsString();
    }

    // We don't have Guava here
    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            hexChars[i * 2] = hexArray[v >>> 4];
            hexChars[i * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    // We don't have Guava here
    private static byte[] hexToBytes(String s) {
        if (s.length() % 2 != 0) {
            throw new IllegalArgumentException("Invalid hexadecimal string");
        }
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
}
