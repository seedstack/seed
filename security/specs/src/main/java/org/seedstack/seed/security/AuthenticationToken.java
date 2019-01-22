/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security;

import java.io.Serializable;

/**
 * An <code>AuthenticationToken</code> is a consolidation of an account's principals
 * and supporting credentials submitted by a user during an authentication
 * attempt.
 * <p>
 * Because applications represent user data and credentials in different ways,
 * implementations of this interface are application-specific. You are free to
 * acquire a user's principals and credentials however you wish (e.g. web form,
 * Swing form, fingerprint identification, etc) and then submit them to the
 * framework in the form of an implementation of this interface.
 * <p>
 * If your application's authentication process is username/password based (like
 * most), instead of implementing this interface yourself, take a look at the
 * {@link UsernamePasswordToken UsernamePasswordToken} class, as it is probably
 * sufficient for your needs.
 */
public interface AuthenticationToken extends Serializable {

    /**
     * Returns the account identity submitted during the authentication process.
     * <p>
     * Most application authentications are username/password based and have
     * this object represent a username. If this is the case for your
     * application, take a look at the {@link UsernamePasswordToken
     * UsernamePasswordToken}, as it is probably sufficient for your use.
     * <p>
     * Ultimately, the object returned is application specific and can represent
     * any account identity.
     *
     * @return the account identity submitted during the authentication process.
     * @see UsernamePasswordToken
     */
    Object getPrincipal();

    /**
     * Returns the credentials submitted by the user during the authentication
     * process that verifies the submitted {@link #getPrincipal() account
     * identity}.
     * <p>
     * Most application authentications are username/password based and have
     * this object represent a submitted password. If this is the case for your
     * application, take a look at the {@link UsernamePasswordToken
     * UsernamePasswordToken}, as it is probably sufficient for your use.
     * <p>
     * Ultimately, the credentials Object returned is application specific and
     * can represent any credential mechanism.
     *
     * @return the credential submitted by the user during the authentication
     * process.
     */
    Object getCredentials();

}
