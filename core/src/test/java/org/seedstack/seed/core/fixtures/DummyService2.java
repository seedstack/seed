/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.fixtures;

import org.seedstack.seed.Logging;

public class DummyService2 implements Service2 {

    @Logging
    org.slf4j.Logger logger;

    @Override
    public void service2() {
        logger.info("ceci est le service : {} ", this.getClass().getCanonicalName());
    }

}
