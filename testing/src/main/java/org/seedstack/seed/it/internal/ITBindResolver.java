/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.it.internal;

import org.seedstack.seed.it.ITBind;
import org.seedstack.shed.reflect.StandardAnnotationResolver;

class ITBindResolver extends StandardAnnotationResolver<Class<?>, ITBind> {
    static ITBindResolver INSTANCE = new ITBindResolver();

    private ITBindResolver() {
        // no external instantiation allowed
    }
}
