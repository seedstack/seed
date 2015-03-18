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
import org.seedstack.seed.persistence.tinkerpop.sample.Person;
import org.seedstack.seed.persistence.tinkerpop.sample.PersonRepository;
import org.seedstack.seed.transaction.api.Transactional;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SeedITRunner.class)
public class SimpleTransactionIT {
    @Inject
    PersonRepository personRepository;

    @Test
    @Transactional
    @Graph("graph1")
    public void transactional_save_with_entitymanager_injection() throws Exception {
        Assertions.assertThat(personRepository).isNotNull();

        final Person peter = personRepository.create("Peter"),
                paul = personRepository.create("Paul"),
                jack = personRepository.create("Jack"),
                johnny = personRepository.create("Johnny");

        personRepository.addKnowsPerson(peter, paul);
        personRepository.addKnowsPerson(paul, jack);
        personRepository.addKnowsPerson(paul, johnny);

        assertThat(peter.getKnowsPeople().iterator().next().getName()).isEqualTo("Paul");
        assertThat(peter.hasFriendOfFriend(paul)).isNull();
        assertThat(peter.hasFriendOfFriend(jack)).isNotNull();
        assertThat(peter.hasFriendOfFriend(johnny)).isNotNull();
    }
}
