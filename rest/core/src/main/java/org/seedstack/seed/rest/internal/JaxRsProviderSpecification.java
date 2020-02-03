/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.internal;

import javax.ws.rs.ext.Provider;
import org.kametic.specifications.AbstractSpecification;
import org.seedstack.shed.reflect.AnnotationPredicates;

/**
 * Matches classes annotated by {@link javax.ws.rs.ext.Provider}.
 */
class JaxRsProviderSpecification extends AbstractSpecification<Class<?>> {
    static final JaxRsProviderSpecification INSTANCE = new JaxRsProviderSpecification();

    private JaxRsProviderSpecification() {
        // no instantiation allowed
    }

    @Override
    public boolean isSatisfiedBy(Class<?> candidate) {
        return AnnotationPredicates.elementAnnotatedWith(Provider.class, false).test(candidate);
    }
}
