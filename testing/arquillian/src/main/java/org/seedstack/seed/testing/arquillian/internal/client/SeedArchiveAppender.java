/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.testing.arquillian.internal.client;

import org.jboss.arquillian.container.test.spi.client.deployment.AuxiliaryArchiveAppender;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.seedstack.seed.testing.arquillian.internal.InjectionTestEnricher;

/**
 * Arquillian AuxiliaryArchiveAppender to add SeedStack Arquillian support to deployed apps.
 */
public class SeedArchiveAppender implements AuxiliaryArchiveAppender {
    @Override
    public Archive<?> createAuxiliaryArchive() {
        return ShrinkWrap.create(JavaArchive.class, "seed-arquillian-support.jar")
                .addPackages(false, "org.seedstack.seed.testing.arquillian.internal.client")
                .addClass(InjectionTestEnricher.class);
    }
}
