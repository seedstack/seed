/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.validation.internal;

import org.seedstack.seed.validation.internal.pojo.DummyServiceFieldValidationOK;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.seedstack.seed.it.SeedITRunner;

import javax.inject.Inject;

@RunWith(SeedITRunner.class)
public class ValidationPlugin_ValidationOkIT {

    @Inject
    DummyServiceFieldValidationOK serviceField;

    @Test
    public void trigger() {
        Assertions.assertThat(serviceField).isNotNull();
    }

}
