/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.security;

/***
 * Possible CRUD actions that will be taken into account for CRUD interceptors
 */
public enum CrudAction {
    CREATE("create"), UPDATE("update"), READ("read"), DELETE("delete");

    private String verb;

    CrudAction(String verb) {
        this.verb = verb;
    }

    public String getVerb() {
        return this.verb;
    }
}
