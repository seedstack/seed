/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.persistence.tinkerpop;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.seedstack.seed.it.SeedITRunner;
import org.seedstack.seed.persistence.tinkerpop.api.Graph;
import org.seedstack.seed.persistence.tinkerpop.api.GraphExceptionHandler;
import org.seedstack.seed.persistence.tinkerpop.sample.Graph2ExceptionHandler;
import org.seedstack.seed.persistence.tinkerpop.sample.Person;
import org.seedstack.seed.persistence.tinkerpop.sample.PersonRepository;
import org.seedstack.seed.transaction.api.Transactional;

import javax.inject.Inject;
import javax.inject.Named;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SeedITRunner.class)
public class ExceptionHandlerIT {
    @Inject
    PersonRepository personRepository;

    @Inject
    @Named("graph2")
    GraphExceptionHandler graphExceptionHandler;

    @Test
    @Transactional
    @Graph("graph2")
    public void error_handler() {
        Person roger = personRepository.create("Roger");

        Assertions.assertThat(((Graph2ExceptionHandler) graphExceptionHandler).hasHandled()).isFalse();
        personRepository.addKnowsPersonWithException(roger, personRepository.create("Robert"));
        assertThat(((Graph2ExceptionHandler) graphExceptionHandler).hasHandled()).isTrue();
    }
}
