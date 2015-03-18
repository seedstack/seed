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

import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import org.seedstack.seed.persistence.inmemory.api.InMemory;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * @author redouane.loulou@ext.mpsa.com
 */
class InMemoryTypeListener implements TypeListener  {
	private final InMemoryTransactionLink inMemoryTransactionLink;

	InMemoryTypeListener(InMemoryTransactionLink inMemoryTransactionLink) {
		this.inMemoryTransactionLink = inMemoryTransactionLink;
	}

	@Override
	public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter) {
        for (Class<?> c = type.getRawType(); c != Object.class; c = c.getSuperclass()) {
            for (Field field : c.getDeclaredFields()) {
              if (Map.class.isAssignableFrom(field.getType()) && ( field.getAnnotation(InMemory.class)) != null) {
                    encounter.register(new InMemoryMapMembersInjector<I>(field, inMemoryTransactionLink));
                }
            }
        }

	}

}
