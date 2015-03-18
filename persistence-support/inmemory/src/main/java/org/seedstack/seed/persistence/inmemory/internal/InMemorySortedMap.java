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
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import com.google.common.collect.Maps;

/**
 * @author epo.jemba@ext.mpsa.com
 */
class InMemorySortedMap<K,V> implements SortedMap<K, V> {
	private static final Map<String, SortedMap<?, ?>> MAP = Maps.newConcurrentMap();

	private final InMemoryTransactionLink inMemoryTransactionLink;

	InMemorySortedMap(InMemoryTransactionLink inMemoryTransactionLink) {
		this.inMemoryTransactionLink = inMemoryTransactionLink;
	}

    @SuppressWarnings("unchecked")
	SortedMap<K, V> getCurrentSortedMap(){
		if(!MAP.containsKey(inMemoryTransactionLink.get())){
				MAP.put(inMemoryTransactionLink.get(), new TreeMap<K, V>());
		}
		return (SortedMap<K, V>) MAP.get(inMemoryTransactionLink.get());
	}

	@Override
	public int size() {
		return getCurrentSortedMap().size();
	}

	@Override
	public boolean isEmpty() {
		return getCurrentSortedMap().isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return getCurrentSortedMap().containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return getCurrentSortedMap().containsValue(value);
	}

	@Override
	public V get(Object key) {
		return getCurrentSortedMap().get(key);
	}

	@Override
	public V put(K key, V value) {
		return getCurrentSortedMap().put(key, value);
	}

	@Override
	public V remove(Object key) {
		return getCurrentSortedMap().remove(key);
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		getCurrentSortedMap().putAll(m);
	}

	@Override
	public void clear() {
		getCurrentSortedMap().clear();
	}

	@Override
	public Comparator<? super K> comparator() {
		return getCurrentSortedMap().comparator();
	}

	@Override
	public SortedMap<K, V> subMap(K fromKey, K toKey) {
		return getCurrentSortedMap().subMap(fromKey, toKey);
	}

	@Override
	public SortedMap<K, V> headMap(K toKey) {
		return getCurrentSortedMap().headMap(toKey);
	}

	@Override
	public SortedMap<K, V> tailMap(K fromKey) {
		return getCurrentSortedMap().tailMap(fromKey);
	}

	@Override
	public K firstKey() {
		return getCurrentSortedMap().firstKey();
	}

	@Override
	public K lastKey() {
		return getCurrentSortedMap().lastKey();
	}

	@Override
	public Set<K> keySet() {
		return getCurrentSortedMap().keySet();
	}

	@Override
	public Collection<V> values() {
		return getCurrentSortedMap().values();
	}

	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet() {
		return getCurrentSortedMap().entrySet();
	}

}
