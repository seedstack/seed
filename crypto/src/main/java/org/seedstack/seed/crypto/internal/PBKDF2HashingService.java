/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.crypto.internal;

import org.seedstack.seed.core.api.SeedException;
import org.seedstack.seed.crypto.api.Hash;
import org.seedstack.seed.crypto.api.HashingService;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

class PBKDF2HashingService implements HashingService {
    private static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA1";

    private static final int SALT_BYTE_SIZE = 24;
    private static final int HASH_BYTE_SIZE = 24;
    private static final int PBKDF2_ITERATIONS = 1000;

    @Override
    public Hash createHash(String toHash) {
        return createHash(toHash.toCharArray());
    }

    @Override
    public Hash createHash(char[] toHash) {
        // Generate a random salt
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_BYTE_SIZE];
        random.nextBytes(salt);

        // Hash the password
        byte[] hash;
        hash = pbkdf2(toHash, salt);

        return new Hash(hash, salt);
    }

    @Override
    public boolean validatePassword(String password, Hash correctHash) {
        return validatePassword(password.toCharArray(), correctHash);
    }

    @Override
    public boolean validatePassword(char[] password, Hash correctHash) {
        // Compute the hash of the provided password, using the same salt
        byte[] testHash = pbkdf2(password, correctHash.getSalt());
        // Compare the hashes in constant time. The password is correct if both hashes match.
        return ByteArrays.slowEquals(correctHash.getHash(), testHash);
    }

    /**
     * Computes the PBKDF2 hash of a password.
     * 
     * @param password the password to hash.
     * @param salt the salt
     * @return the PBDKF2 hash of the password
     */
    private byte[] pbkdf2(char[] password, byte[] salt) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, PBKDF2_ITERATIONS, HASH_BYTE_SIZE * 8);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
            return skf.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException e) {
            throw SeedException.wrap(e, CryptoErrorCodes.UNEXPECTED_EXCEPTION);
        } catch (InvalidKeySpecException e) {
            throw SeedException.wrap(e, CryptoErrorCodes.UNEXPECTED_EXCEPTION);
        }
    }

}
