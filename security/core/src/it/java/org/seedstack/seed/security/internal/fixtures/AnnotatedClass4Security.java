/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.internal.fixtures;

import org.seedstack.seed.it.ITBind;
import org.seedstack.seed.security.RequiresPermissions;
import org.seedstack.seed.security.RequiresRoles;

@ITBind
public class AnnotatedClass4Security {
    final String place = "coruscant";

    @RequiresRoles("jedi")
    public boolean callTheForce() {
        return true;
    }

    @RequiresPermissions("academy:teach")
    public boolean teach() {
        return true;
    }
}