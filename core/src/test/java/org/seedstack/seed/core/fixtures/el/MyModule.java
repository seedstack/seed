/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.fixtures.el;

import com.google.inject.AbstractModule;
import org.seedstack.seed.Install;
import org.seedstack.seed.core.internal.el.ELBinder;

@Install
public class MyModule extends AbstractModule {

    @Override
    protected void configure() {
        new ELBinder(this.binder())
                .bindELAnnotation(PreEL.class, ELBinder.ExecutionPolicy.BEFORE)
                .bindELAnnotation(PostEL.class, ELBinder.ExecutionPolicy.AFTER)
                .bindELAnnotation(AnnotationWithoutValue.class);
    }
}
