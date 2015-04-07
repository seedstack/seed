/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.validation.internal.pojo;

import javax.validation.constraints.AssertFalse;
import javax.validation.constraints.Max;
import javax.validation.constraints.Size;

/**
 * @author pierre.thirouin@ext.mpsa.com
 *         14/10/2014
 */
public class Pojo {

    @Size(min = 5)
    private String name = "yoda";

    @Max(100)
    private int age = 10000;

    @AssertFalse
    private boolean dead = true;

    public static enum State {
        VALID,
        INVALID
    }


    public Pojo(State state) {
        if (State.INVALID.equals(state)) {
            name = "yoda";
            age = 10000;
            dead = true;
        } else {
            name = "yodaa";
            age = 99;
            dead = false;
        }
    }

    public String getName() {
        return this.name;
    }
    
}
