/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package some.other.pkg;

import com.google.inject.AbstractModule;
import org.seedstack.seed.Install;

@Install
class ForeignModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ForeignClass.class);
    }

}
