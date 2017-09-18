/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;

public abstract class ClassConfiguration<T> {
    private final Class<T> targetClass;
    private final Map<String, String> map;

    public static <T> ClassConfiguration<T> of(Class<T> targetClass, Map<String, String> source) {
        return new ClassConfiguration<T>(targetClass, source) {
        };
    }

    public static <T> ClassConfiguration<T> of(Class<T> targetClass, String... keyValuePairs) {
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < keyValuePairs.length - 1; i += 2) {
            map.put(keyValuePairs[i], keyValuePairs[i + 1]);
        }
        return new ClassConfiguration<T>(targetClass, map) {
        };
    }

    public static <T> ClassConfiguration<T> empty(Class<T> targetClass) {
        return new ClassConfiguration<T>(targetClass, new HashMap<>()) {
        };
    }

    private ClassConfiguration(Class<T> targetClass, Map<String, String> source) {
        this.targetClass = targetClass;
        this.map = new HashMap<>(source);
    }

    public int size() {
        return map.size();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public boolean containsKey(String key) {
        return map.containsKey(key);
    }

    public String get(String key) {
        return map.get(key);
    }

    public Set<String> keySet() {
        return Collections.unmodifiableSet(map.keySet());
    }

    public Collection<String> values() {
        return Collections.unmodifiableCollection(map.values());
    }

    public Set<Map.Entry<String, String>> entrySet() {
        return Collections.unmodifiableSet(map.entrySet());
    }

    public String getOrDefault(String key, String defaultValue) {
        return map.getOrDefault(key, defaultValue);
    }

    public void forEach(BiConsumer<? super String, ? super String> action) {
        map.forEach(action);
    }

    public Map<String, String> asMap() {
        return Collections.unmodifiableMap(map);
    }

    public ClassConfiguration<T> merge(ClassConfiguration<T> other) {
        if (!targetClass.isAssignableFrom(other.targetClass)) {
            throw new IllegalArgumentException("Cannot merge class configurations: " + targetClass.getName() + " is not assignable to " + other.targetClass.getName());
        }
        map.putAll(other.map);
        map.values().removeIf(Objects::isNull);
        return this;
    }

    public Class<T> getTargetClass() {
        return targetClass;
    }
}
