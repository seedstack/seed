/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.cli.internal;

import java.lang.reflect.AnnotatedElement;
import org.seedstack.seed.cli.WithCliCommand;
import org.seedstack.shed.reflect.StandardAnnotationResolver;

class WithCliCommandResolver extends StandardAnnotationResolver<AnnotatedElement, WithCliCommand> {
    static WithCliCommandResolver INSTANCE = new WithCliCommandResolver();

    private WithCliCommandResolver() {
        // no external instantiation allowed
    }
}
