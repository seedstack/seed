/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.persistence.inmemory.internal;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javolution.util.FastMap;

import com.google.common.collect.Maps;

/**
 * @author epo.jemba@ext.mpsa.com
 */
class InMemoryMap<K,V> implements Map<K, V> {
	private static final Map<String, Map<?, ?>> MAP = Maps.newConcurrentMap();

	private final InMemoryTransactionLink inMemoryTransactionLink;

	InMemoryMap(InMemoryTransactionLink inMemoryTransactionLink) {
		this.inMemoryTransactionLink = inMemoryTransactionLink;
	}


    @SuppressWarnings("unchecked")
	Map<K, V> getCurrentMap(){
		if(!MAP.containsKey(inMemoryTransactionLink.get())){
				MAP.put(inMemoryTransactionLink.get(), new FastMap<K, V>())  ;
		}
		return (Map<K, V>) MAP.get(inMemoryTransactionLink.get());
	}

	@Override
	public int size() {
		return getCurrentMap().size();
	}

	@Override
	public boolean isEmpty() {
		return getCurrentMap().isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return getCurrentMap().containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return getCurrentMap().containsValue(value);
	}

	@Override
	public V get(Object key) {
		return getCurrentMap().get(key);
	}

	@Override
	public V put(K key, V value) {
		return getCurrentMap().put(key, value);
	}

	@Override
	public V remove(Object key) {
		return getCurrentMap().remove(key);
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		getCurrentMap().putAll(m);
	}

	@Override
	public void clear() {
		getCurrentMap().clear();
	}

	@Override
	public Set<K> keySet() {
		return getCurrentMap().keySet();
	}

	@Override
	public Collection<V> values() {
		return getCurrentMap().values();
	}

	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet() {
		return getCurrentMap().entrySet();
	}

}
