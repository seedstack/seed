/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.internal.fixtures.data;

import org.seedstack.seed.security.data.Secured;

public interface DummyService {

    @Secured
    Dummy service1(@Secured Dummy d1, Dummy d2, Dummy d3);

    Dummy service2(Dummy d4);

}
