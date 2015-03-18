/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
/**
 *
 */
package org.seedstack.seed.spring;

import com.google.inject.CreationException;
import org.seedstack.seed.it.AbstractSeedIT;
import org.seedstack.seed.it.api.Expect;
import org.seedstack.seed.spring.api.WithApplicationContexts;
import org.seedstack.seed.spring.fixtures.Service;
import org.junit.Test;

import javax.inject.Inject;
import javax.inject.Named;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SpringTransactionHandlerIT
 *
 * @author redouane.loulou@ext.mpsa.com
 */
@WithApplicationContexts({"META-INF/spring/first-context.xml", "META-INF/spring/second-context.xml"})
@Expect(CreationException.class)
public class AllSpringContextsIT extends AbstractSeedIT {
    @Inject
    @Named("service1")
    Service service1;

    @Inject
    @Named("service2")
    Service service2;

    @Test
    public void specified_spring_contexts_should_be_loaded() {
        assertThat(service1).isNotNull();
        assertThat(service2).isNotNull();
    }
}
