/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.ws.internal;

import com.google.inject.Injector;
import com.sun.xml.wss.RealmAuthenticationAdapter;
import com.sun.xml.wss.XWSSecurityException;

import javax.inject.Inject;
import javax.security.auth.Subject;

/**
 * This {@link com.sun.xml.wss.RealmAuthenticationAdapter} delegates to another {@link com.sun.xml.wss.RealmAuthenticationAdapter},
 * (configurable through org.seedstack.seed.ws.realm-authentication-adapter property and made injectable).
 *
 * @author adrien.lauer@mpsa.com
 */
public class SeedRealmAuthenticationAdapterDelegate extends RealmAuthenticationAdapter {
    @Inject
    private static Injector injector;

    @Override
    public boolean authenticate(Subject callerSubject, String username, String password) throws XWSSecurityException {
        return injector.getInstance(RealmAuthenticationAdapter.class).authenticate(callerSubject, username, password);
    }
}
