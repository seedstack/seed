/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.mail.assertions;

import org.seedstack.seed.core.api.SeedException;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Condition;
import org.assertj.core.internal.Objects;

import javax.mail.NoSuchProviderException;
import javax.mail.Provider;
import javax.mail.Session;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Assertion class over mail session.
 *
 * @param <S> the assertion class
 * @author aymen.benhmida@ext.mpsa.com
 */
public class MailSessionAssertions<S extends Session> extends AbstractAssert<MailSessionAssertions<Session>, Session> {
    private Session session;

    /**
     * Create the assertion.
     *
     * @param session the session to assert.
     */
    public MailSessionAssertions(S session) {
        super(session, MailSessionAssertions.class);
        this.session = session;
    }

    /**
     * Assert that the session is not null.
     *
     * @return the assertion for chaining.
     */
    public MailSessionAssertions isNotNull() {
        assertThat(session).isNotNull();
        return this;
    }

    /**
     * Assert that the session has properties.
     *
     * @return the assertion for chaining.
     */
    public MailSessionAssertions hasProperties() {
        Objects.instance().assertNotNull(info, actual);
        assertThat(session).has(new Condition<Session>() {
            @Override
            public boolean matches(Session value) {
                return value.getProperties() != null;
            }
        });
        return this;
    }

    /**
     * Assert that the session has specific property.
     *
     * @param property the property to assert.
     * @return the assertion for chaining.
     */
    public MailSessionAssertions hasProperty(final String property) {
        Objects.instance().assertNotNull(info, actual);
        assertThat(session).has(new Condition<Session>() {
            @Override
            public boolean matches(Session value) {
                return value.getProperty(property) != null;
            }
        });
        return this;
    }

    /**
     * Assert that the session has a transport.
     *
     * @return the assertion for chaining.
     */
    public MailSessionAssertions hasTransport() {
        Objects.instance().assertNotNull(info, actual);
        assertThat(session).has(new Condition<Session>() {
            @Override
            public boolean matches(Session value) {
                try {
                    return value.getTransport() != null;
                } catch (NoSuchProviderException e) {
                    throw SeedException
                            .wrap(e, MailAssertionsErrorCodes.NO_MAIL_PROVIDER_FOUND)
                            .put("more", "\n" + e.getMessage());
                }
            }
        });
        return this;
    }

    /**
     * Assert that the session has a specific transport.
     *
     * @param provider the transport provider to assert.
     * @return the assertion for chaining.
     */
    public MailSessionAssertions hasTransport(final Provider provider) {
        Objects.instance().assertNotNull(info, actual);
        assertThat(session).has(new Condition<Session>() {
            @Override
            public boolean matches(Session value) {
                try {
                    return value.getTransport(provider) != null;
                } catch (NoSuchProviderException e) {
                    throw SeedException
                            .wrap(e, MailAssertionsErrorCodes.NO_SUCH_PROVIDER_FOUND)
                            .put("more", "\n" + e.getMessage());
                }
            }
        });
        return this;
    }
}
