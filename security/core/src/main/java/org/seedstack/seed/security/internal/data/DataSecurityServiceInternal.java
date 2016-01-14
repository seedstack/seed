/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.internal.data;

import com.google.inject.Injector;
import org.seedstack.seed.security.data.DataSecurityService;
import org.seedstack.seed.security.internal.securityexpr.SecurityExpressionInterpreter;
import org.seedstack.seed.security.spi.data.DataSecurityHandler;
import org.kametic.universalvisitor.UniversalVisitor;
import org.kametic.universalvisitor.api.Filter;


import javax.inject.Inject;
import java.lang.reflect.Field;
import java.util.Map;

/**
 * Implementation of the DataSecurityService with the UniversalVisitor library.
 *
 * @author epo.jemba@ext.mpsa.com
 * @author pierre.thirouin@ext.mpsa.com
 */
class DataSecurityServiceInternal implements DataSecurityService {

    @Inject
    private SecurityExpressionInterpreter securityExpressionInterpreter;

    @Inject
    private Map<Object, DataSecurityHandler<?>> securityHandlers;

    @Inject
    private Injector injector;

    @Override
    public <Candidate> void secure(Candidate candidate) {

        DataSecurityMapper dataSecurityMapper = new DataSecurityMapper(securityHandlers, securityExpressionInterpreter, injector);

        UniversalVisitor universalVisitor = new UniversalVisitor();

        universalVisitor.visit(candidate, new SyntheticPredicate(), dataSecurityMapper);
    }

    private static class SyntheticPredicate implements Filter {

        @Override
        public boolean retains(Field input) {

            return !input.isSynthetic();
        }

    }


}
