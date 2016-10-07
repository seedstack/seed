/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.internal;

import org.kametic.specifications.AbstractSpecification;
import org.seedstack.seed.core.utils.BaseClassSpecifications;

import javax.ws.rs.ext.Provider;

import static org.seedstack.seed.core.utils.BaseClassSpecifications.classAnnotatedWith;


public class JaxRsProviderSpecification extends AbstractSpecification<Class<?>> {
    @Override
    @SuppressWarnings("unchecked")
    public boolean isSatisfiedBy(Class<?> candidate) {
        return BaseClassSpecifications.or(classAnnotatedWith(Provider.class)).isSatisfiedBy(candidate);
    }
}
