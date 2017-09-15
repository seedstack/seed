/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.internal.fixtures;

import org.seedstack.seed.it.ITBind;
import org.seedstack.seed.security.RequiresCrudPermissions;
import org.seedstack.seed.security.internal.fixtures.annotations.CREATE;
import org.seedstack.seed.security.internal.fixtures.annotations.DELETE;
import org.seedstack.seed.security.internal.fixtures.annotations.READ;
import org.seedstack.seed.security.internal.fixtures.annotations.UPDATE;

@ITBind
public class AnnotatedCrudMethods4Security {

    @DELETE
    @RequiresCrudPermissions("crudTest")
    public boolean delete() {
        return true;
    }

    @READ
    @RequiresCrudPermissions("crudTest")
    public boolean read() {
        return true;
    }

    @UPDATE
    @RequiresCrudPermissions("crudTest")
    public boolean update() {
        return true;
    }

    @CREATE
    @RequiresCrudPermissions("crudTest")
    public boolean create() {
        return true;
    }

    // Empty
    public boolean none() {
        return true;
    }

}
