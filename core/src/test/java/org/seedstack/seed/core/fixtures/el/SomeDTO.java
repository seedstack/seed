/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.fixtures.el;

import org.seedstack.seed.Bind;

@Bind
public class SomeDTO {
    private String message = "hello";

    @PreEL("${1+1}")
    @PostEL("${1+1}")
    public void setMessage(String message) {
        this.message = message;
    }

    @PreEL("${zz}")
    public void failingMethod() {
        // do nothing
    }
}
