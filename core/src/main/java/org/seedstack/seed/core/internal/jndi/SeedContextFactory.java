/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.jndi;

import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

/**
 * JNDI context factory for {@link JndiContext} context.
 */
public class SeedContextFactory implements InitialContextFactory {
    @Override
    public Context getInitialContext(final Hashtable environment) throws NamingException {
        return new JndiContext(environment);
    }
}