/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.persistence.jpa.sample;


import org.seedstack.seed.it.api.ITBind;

import javax.inject.Inject;
import javax.persistence.EntityManager;

@ITBind
public class Item1Repository {
    private EntityManager entityManager;

    @Inject
    public Item1Repository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void save(Item1 item) {
        entityManager.persist(item);
    }
}
