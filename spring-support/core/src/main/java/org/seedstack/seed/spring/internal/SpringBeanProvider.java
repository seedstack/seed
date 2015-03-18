/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.spring.internal;

import com.google.inject.Provider;
import org.springframework.beans.factory.BeanFactory;

import static com.google.common.base.Preconditions.checkNotNull;

class SpringBeanProvider<T> implements Provider<T> {
    final BeanFactory beanFactory;
    final Class<T> type;
    final String name;

    SpringBeanProvider(Class<T> type, String name, BeanFactory beanFactory) {
        this.type = checkNotNull(type, "type");
        this.name = checkNotNull(name, "name");
        this.beanFactory = beanFactory;
    }

    @Override
    public T get() {
        return type.cast(beanFactory.getBean(name));
    }
}