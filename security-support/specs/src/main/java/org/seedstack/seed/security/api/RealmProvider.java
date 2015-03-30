/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.api;

import java.util.Set;

/**
 * A realm provider is able to provide all the realms of the application
 * 
 * @author yves.dautremay@mpsa.com
 * @deprecated use methods in securitySupport getRoles()
 */
@Deprecated
public interface RealmProvider extends SecuritySupport {

    /**
     * Gives all known realms of the application
     * 
     * @return a Set of Realm
     * @deprecated use methods in securitySupport getRoles()
     */
    Set<Realm> provideRealms();
}
