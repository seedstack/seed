/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.fixtures.validation;

public class CustomPojo {
    @CheckCase(CaseMode.UPPER)
    private final String item;

    public CustomPojo(String item) {
        this.item = item;
    }
}
