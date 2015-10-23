/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.internal;

import org.kametic.specifications.AbstractSpecification;
import org.seedstack.seed.core.utils.BaseClassSpecifications;

import javax.ws.rs.ext.*;

import static org.seedstack.seed.core.utils.BaseClassSpecifications.classAnnotatedWith;
import static org.seedstack.seed.core.utils.BaseClassSpecifications.classImplements;

/**
 * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
 */
public class JaxRsProviderSpecification extends AbstractSpecification<Class<?>> {
    @Override
    public boolean isSatisfiedBy(Class<?> candidate) {
        //noinspection unchecked
        return BaseClassSpecifications.or(
                classAnnotatedWith(Provider.class).and(classImplements(MessageBodyWriter.class)),
                classAnnotatedWith(Provider.class).and(classImplements(ContextResolver.class)),
                classAnnotatedWith(Provider.class).and(classImplements(MessageBodyReader.class)),
                classAnnotatedWith(Provider.class).and(classImplements(ExceptionMapper.class))
        ).isSatisfiedBy(candidate);
    }
}
