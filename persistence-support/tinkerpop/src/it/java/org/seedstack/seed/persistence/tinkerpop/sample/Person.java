/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.persistence.tinkerpop.sample;

import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.annotations.gremlin.GremlinGroovy;
import com.tinkerpop.frames.annotations.gremlin.GremlinParam;

public interface Person {
    @Property("name")
    public String getName();

    @Property("name")
    public void setName(String name);

    @Adjacency(label = "knows")
    public Iterable<Person> getKnowsPeople();

    @Adjacency(label = "knows")
    public void addKnowsPerson(final Person person);

    @GremlinGroovy("it.out('knows').out('knows').dedup")
    public Iterable<Person> getFriendsOfAFriend();

    @GremlinGroovy(value="it.out('knows').out('knows').filter{it.name == person.name}")
    public Person hasFriendOfFriend(@GremlinParam("person") Person person);
}