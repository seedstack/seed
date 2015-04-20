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
import org.seedstack.seed.persistence.jpa.api.JpaUnit;
import org.seedstack.seed.persistence.jpa.sample.Item1;
import org.seedstack.seed.persistence.jpa.sample.Item1Repository;
import org.seedstack.seed.transaction.api.Transactional;

import javax.inject.Inject;

@Transactional
@JpaUnit("unit1")
@RunWith(SeedITRunner.class)
public class SimpleTransactionIT {

    @Inject
    Item1Repository item1Repository;

    @Test
    public void transactional_save_with_entitymanager_injection() throws Exception {
        Assertions.assertThat(item1Repository).isNotNull();

        Item1 item1 = new Item1();
        item1.setID(10L);
        item1.setName("item1Name");

        item1Repository.save(item1);

        Assertions.assertThat(item1.getID()).isEqualTo(10L);

        Item1 item2 = new Item1();
        item2.setID(20L);
        item2.setName("item1Name");
        item1Repository.save(item2);
        Assertions.assertThat(item2.getID()).isEqualTo(20L);
    }
}
