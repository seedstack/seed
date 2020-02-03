/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.fixtures;

import org.seedstack.seed.Bind;
import org.seedstack.seed.security.RequiresCrudPermissions;
import org.seedstack.seed.security.fixtures.annotations.READ;
import org.seedstack.seed.security.fixtures.annotations.CREATE;
import org.seedstack.seed.security.fixtures.annotations.DELETE;
import org.seedstack.seed.security.fixtures.annotations.UPDATE;

@Bind
@RequiresCrudPermissions("crudTest")
public class AnnotatedCrudClass4Security {

    @DELETE
    public boolean delete() {
        return true;
    }

    @READ
    public boolean read() {
        return true;
    }

    @UPDATE
    public boolean update() {
        return true;
    }

    @CREATE
    public boolean create() {
        return true;
    }

    // Empty
    public boolean none() {
        return true;
    }

}
