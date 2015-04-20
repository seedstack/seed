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
import org.seedstack.seed.persistence.jpa.sample.Item2;
import org.seedstack.seed.persistence.jpa.sample.Item2Repository;
import org.seedstack.seed.transaction.api.Propagation;
import org.seedstack.seed.transaction.api.Transactional;

import javax.inject.Inject;

import static org.junit.Assert.fail;

@RunWith(SeedITRunner.class)
public class NestedTransactionsIT {

    @Inject
    Item1Repository item1Repository;

    @Inject
    Item2Repository item2Repository;

    @Test(expected = IllegalArgumentException.class)
    @Transactional
    @JpaUnit("unit1")
    public void access_to_unit2_with_unit1_transaction_should_fail() throws Exception {
        Item2 item2 = new Item2();
        item2.setID(10L);
        item2.setName("item2Name");
        item2Repository.save(item2);
        fail("should have failed");
    }

    @Test
    @Transactional
    @JpaUnit("unit1")
    public void access_to_unit2_with_new_transaction_should_succeed() throws Exception {
        Item1 item1 = new Item1();
        item1.setID(20L);
        item1.setName("item1Name");
        item1Repository.save(item1);
        Assertions.assertThat(item1.getID()).isEqualTo(20L);

        access_to_unit2_with_new_transaction();

        access_to_unit1_with_current_transaction();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @JpaUnit("unit2")
    protected void access_to_unit2_with_new_transaction() {
        Item2 item2 = new Item2();
        item2.setID(30L);
        item2.setName("item2Name");
        item2Repository.save(item2);
        Assertions.assertThat(item2.getID()).isEqualTo(30L);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    @JpaUnit("unit1")
    protected void access_to_unit1_with_current_transaction() {
        Item1 item1 = new Item1();
        item1.setID(40L);
        item1.setName("item1Name");
        item1Repository.save(item1);
        Assertions.assertThat(item1.getID()).isEqualTo(40L);
    }
}
