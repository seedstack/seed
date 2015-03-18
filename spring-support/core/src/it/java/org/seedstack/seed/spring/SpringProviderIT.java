/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.spring;


import org.seedstack.seed.it.SeedITRunner;
import org.seedstack.seed.spring.fixtures.Service;
import org.seedstack.seed.spring.fixtures.SpringRepository;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.inject.Named;

@RunWith(SeedITRunner.class)
public class SpringProviderIT {
    @Inject
    @Named("service")
    Service service;

    @Inject
    @Named("springRepository")
    SpringRepository springRepository;

    @Test
    public void can_get_spring_bean_by_interface_type() {
        Assertions.assertThat(service).isNotNull();
        Assertions.assertThat(service.getFrom()).isEqualTo("spring");
    }

    @Test
    public void scanned_components_are_detected() {
        Assertions.assertThat(springRepository).isNotNull();
        Assertions.assertThat(springRepository.getId()).isEqualTo(1L);
    }
}
