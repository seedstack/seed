/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.fixtures.validation;

import javax.validation.constraints.NotNull;
import org.seedstack.seed.Bind;

@Bind
public class FieldValidationOK {
    @NotNull
    private Object param = new Object();

    public Object getParam() {
        return param;
    }
}
