/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.ws.internal;

import com.google.inject.Injector;
import org.seedstack.seed.core.utils.SeedReflectionUtils;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.server.AbstractMultiInstanceResolver;

import javax.inject.Inject;

class SeedInstanceResolver extends AbstractMultiInstanceResolver<Object> {
    @Inject
    private static Injector injector;

    SeedInstanceResolver(Class<Object> clazz) {
        super(clazz);
    }

    @Override
    public Object resolve(Packet packet) {
        Object instance = injector.getInstance(this.clazz);

        try {
            SeedReflectionUtils.preserveAnnotationsInProxy(instance.getClass());
        } catch (Exception e) {
            throw new RuntimeException("Unable to instantiate WS implementation " + this.clazz.getCanonicalName(), e);
        }

        prepare(instance);

        return instance;
    }
}

