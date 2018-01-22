/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.web.internal.cors;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.thetransactioncompany.cors.CORSFilter;

class WebCORSModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(CORSFilter.class).in(Scopes.SINGLETON);
    }
}
