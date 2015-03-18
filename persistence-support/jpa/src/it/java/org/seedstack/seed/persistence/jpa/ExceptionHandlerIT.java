/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.persistence.jpa;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.seedstack.seed.it.SeedITRunner;
import org.seedstack.seed.persistence.jpa.api.JpaExceptionHandler;
import org.seedstack.seed.persistence.jpa.api.JpaUnit;
import org.seedstack.seed.persistence.jpa.sample.Item3Repository;
import org.seedstack.seed.persistence.jpa.sample.Unit3ExceptionHandler;
import org.seedstack.seed.transaction.api.Transactional;

import javax.inject.Inject;
import javax.inject.Named;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SeedITRunner.class)
public class ExceptionHandlerIT {
    @Inject
    Item3Repository item3Repository;

    @Inject
    @Named("unit3")
    JpaExceptionHandler jpaExceptionHandler;

    @Test
    @Transactional
    @JpaUnit("unit3")
    public void error_handler() {
        Assertions.assertThat(item3Repository).isNotNull();

        Assertions.assertThat(((Unit3ExceptionHandler) jpaExceptionHandler).hasHandled()).isFalse();
        item3Repository.saveWithException();
        assertThat(((Unit3ExceptionHandler) jpaExceptionHandler).hasHandled()).isTrue();
    }
}
