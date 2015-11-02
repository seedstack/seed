/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest;

import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import org.seedstack.seed.Install;
import org.seedstack.seed.rest.fixtures.PersonRepresentation;
import org.seedstack.seed.rest.fixtures.ProductRepresentation;

import java.util.List;

@Install
public class TestModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(new TypeLiteral<List<PersonRepresentation>>() {
        })
                .toInstance(Lists.newArrayList(
                                new PersonRepresentation("foo", "bar"),
                                new PersonRepresentation("toto", "titi")
                        )
                );

        bind(new TypeLiteral<List<ProductRepresentation>>() {
        })
                .toInstance(Lists.newArrayList(
                                new ProductRepresentation("laptop"),
                                new ProductRepresentation("desktop")
                        )
                );
    }
}
