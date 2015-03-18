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
package org.seedstack.seed.persistence.inmemory.internal;

import com.google.inject.MembersInjector;

import java.lang.reflect.Field;

/**
 * @author redouane.loulou@ext.mpsa.com
 */
class InMemoryMapMembersInjector<T> implements MembersInjector<T>  {

    private final Field field;
	private InMemoryTransactionLink inMemoryTransactionLink;

    InMemoryMapMembersInjector(Field field, InMemoryTransactionLink inMemoryTransactionLink) {
        this.field = field;
        this.inMemoryTransactionLink = inMemoryTransactionLink;
        field.setAccessible(true);
    }

    @Override
	public void injectMembers(T instance) {
        try {
            field.set(instance, new InMemoryMap<Object, Object>(inMemoryTransactionLink));
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
	}

}
