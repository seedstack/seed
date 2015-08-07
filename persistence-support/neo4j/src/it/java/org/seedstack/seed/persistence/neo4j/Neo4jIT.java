/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.persistence.neo4j;

import org.junit.Test;
import org.seedstack.seed.core.api.SeedException;
import org.seedstack.seed.it.AbstractSeedIT;
import org.seedstack.seed.persistence.neo4j.api.Neo4jDb;
import org.seedstack.seed.persistence.neo4j.fixtures.Item;
import org.seedstack.seed.persistence.neo4j.fixtures.ItemRepository;
import org.seedstack.seed.persistence.neo4j.fixtures.ItemThrowableHandler;
import org.seedstack.seed.transaction.api.Propagation;
import org.seedstack.seed.transaction.api.Transactional;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;

public class Neo4jIT extends AbstractSeedIT {
    @Inject
    ItemRepository itemRepository;

    @Inject
    ItemThrowableHandler itemThrowableHandler;

    @Test(expected = SeedException.class)
    public void access_outside_transaction() throws Exception {
        Item item1 = new Item();
        item1.setID(1L);
        item1.setName("item1Name");
        itemRepository.save(item1);
    }

    @Test
    @Transactional
    @Neo4jDb("db1")
    public void simple_transaction() throws Exception {
        Item item1 = new Item();
        item1.setID(1L);
        item1.setName("item1Name");
        itemRepository.save(item1);
        assertThat(itemRepository.findById(1L).getName()).isEqualTo("item1Name");

        Item item2 = new Item();
        item2.setID(2L);
        item2.setName("item2Name");
        itemRepository.save(item2);
        assertThat(itemRepository.findById(2L).getName()).isEqualTo("item2Name");
    }

    @Test
    @Transactional
    @Neo4jDb("db1")
    public void nested_transactions() throws Exception {
        Item item1 = new Item();
        item1.setID(3L);
        item1.setName("item3Name");
        itemRepository.save(item1);
        assertThat(itemRepository.findById(3L).getName()).isEqualTo("item3Name");

        access_to_db1_with_current_transaction();
        access_to_db2_with_new_transaction();
    }

    @Test
    @Transactional
    @Neo4jDb("db3")
    public void error_handler() {
        assertThat(itemThrowableHandler.hasHandled()).isFalse();
        itemRepository.saveWithException(new Item());
        assertThat(itemThrowableHandler.hasHandled()).isTrue();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Neo4jDb("db2")
    protected void access_to_db2_with_new_transaction() {
        Item item = new Item();
        item.setID(4L);
        item.setName("item4Name");
        itemRepository.save(item);
        assertThat(itemRepository.findById(4L).getName()).isEqualTo("item4Name");
    }

    @Transactional(propagation = Propagation.MANDATORY)
    @Neo4jDb("db1")
    protected void access_to_db1_with_current_transaction() {
        assertThat(itemRepository.findById(3L).getName()).isEqualTo("item3Name");
    }
}
