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

import org.seedstack.seed.it.api.ITBind;
import com.tinkerpop.frames.FramedGraph;

import javax.inject.Inject;

@ITBind
public class PersonRepository {
    @Inject
    private FramedGraph manager;

    public Person create(String name) {
        Person person = (Person) manager.addVertex(name, Person.class);
        person.setName(name);
        return person;
    }

    public void addKnowsPerson(Person source, Person target) {
        source.addKnowsPerson(target);
    }

    public void addKnowsPersonWithException(Person source, Person target) {
        throw new IllegalStateException("");
    }
}

