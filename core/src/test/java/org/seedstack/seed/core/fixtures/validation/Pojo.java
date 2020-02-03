/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.fixtures.validation;

import javax.validation.constraints.AssertFalse;
import javax.validation.constraints.Max;
import javax.validation.constraints.Size;

public class Pojo {
    @Size(min = 5)
    private String name = "yoda";

    @Max(100)
    private int age = 10000;

    @AssertFalse
    private boolean dead = true;

    public Pojo(State state) {
        if (State.INVALID.equals(state)) {
            createInvalidPojo();
        } else {
            createValidPojo();
        }
    }

    private void createValidPojo() {
        name = "yodaa";
        age = 99;
        dead = false;
    }

    private void createInvalidPojo() {
        name = "yoda";
        age = 10000;
        dead = true;
    }

    public String getName() {
        return this.name;
    }

    public enum State {
        VALID,
        INVALID
    }
}
