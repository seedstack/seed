/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.fixtures.data;


import org.seedstack.seed.security.api.data.Secured;

public class DummyServiceInternal implements DummyService {

    @Override
    public Dummy service1(Dummy d1, @Secured Dummy d2, Dummy d3) {
        return DummyFactory.create(1);
    }

    @Override
    public Dummy service2(Dummy d4) {
        return DummyFactory.create(2);
    }

}
