/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.el.internal.fixtures;

import com.google.inject.AbstractModule;
import org.seedstack.seed.core.api.Install;
import org.seedstack.seed.el.internal.ELBinder;
import org.seedstack.seed.el.internal.fixtureFailing.AnnotationWithoutValue;

/**
 * @author pierre.thirouin@ext.mpsa.com
 *         Date: 01/07/2014
 */
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
