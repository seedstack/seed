/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.persistence.neo4j.fixtures;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.seedstack.seed.it.api.ITBind;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@ITBind
public class ItemRepository {
    @Inject
    private GraphDatabaseService graphDatabaseService;

    public void save(Item item) {
        Node node = graphDatabaseService.createNode();
        node.setProperty("id", item.getID());
        node.setProperty("name", item.getName());

        graphDatabaseService.index().forNodes("items").add(node, "id", node.getProperty("id"));
    }

    public void saveWithException(Item item) {
        throw new IllegalStateException("Failure to save");
    }

    public Item findById(long id) {
        Node node = graphDatabaseService.index().forNodes("items").get("id", id).getSingle();

        Item item = new Item();
        item.setID((Long) node.getProperty("id"));
        item.setName((String) node.getProperty("name"));

        return item;
    }

    public void update(Item item) {
        Node node = graphDatabaseService.index().forNodes("items").get("id", item.getID()).getSingle();
        node.setProperty("name", item.getName());
    }

    public void delete(Item item) {
        graphDatabaseService.index().forNodes("items").get("id", item.getID()).getSingle().delete();
    }

    public List<Item> findAll() {
        List<Item> items = new ArrayList<Item>();
        for (Node node : graphDatabaseService.index().forNodes("items").get("id", "*")) {
            Item item = new Item();
            item.setID((Long) node.getProperty("id"));
            item.setName((String) node.getProperty("name"));
            items.add(item);
        }
        return items;
    }
}
