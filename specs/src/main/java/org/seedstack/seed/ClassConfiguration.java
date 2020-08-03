/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed;

import java.util.*;
import java.util.function.BiConsumer;

/**
 * A key/value based configuration object for a specific target class. The {@link Application#getConfiguration(Class)}
 * method returns such objects as a way to find properties attached to particular classes.
 *
 * @param <T> the type of the target object.
 */
public abstract class ClassConfiguration<T> {
    private final Class<T> targetClass;
    private final Map<String, String> map;

    private ClassConfiguration(Class<T> targetClass, Map<String, String> source) {
        this.targetClass = targetClass;
        this.map = new HashMap<>(source);
    }

    /**
     * Create a class configuration for the specified class, with the specified properties.
     *
     * @param targetClass the class this configuration refers to.
     * @param source      the source properties.
     * @param <T>         the type of the target object.
     * @return the class configuration object.
     */
    public static <T> ClassConfiguration<T> of(Class<T> targetClass, Map<String, String> source) {
        return new ClassConfiguration<T>(targetClass, source) {
        };
    }

    /**
     * Create a class configuration for the specified class, with the specified properties.
     *
     * @param targetClass   the class this configuration refers to.
     * @param keyValuePairs key/value pairs forming properties .
     * @param <T>           the type of the target object.
     * @return the class configuration object.
     */
    public static <T> ClassConfiguration<T> of(Class<T> targetClass, String... keyValuePairs) {
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < keyValuePairs.length - 1; i += 2) {
            map.put(keyValuePairs[i], keyValuePairs[i + 1]);
        }
        return new ClassConfiguration<T>(targetClass, map) {
        };
    }

    /**
     * Create an empty class configuration for the specified class.
     *
     * @param targetClass the class this configuration refers to.
     * @param <T>         the type of the target object.
     * @return the class configuration object.
     */
    public static <T> ClassConfiguration<T> empty(Class<T> targetClass) {
        return new ClassConfiguration<T>(targetClass, new HashMap<>()) {
        };
    }

    /**
     * Returns the number of key/value pairs.
     *
     * @return the size of the class configuration.
     */
    public int size() {
        return map.size();
    }

    /**
     * Returns if the class configuration is empty or not.
     *
     * @return true if empty, false otherwise.
     */
    public boolean isEmpty() {
        return map.isEmpty();
    }

    /**
     * Returns if the class configuration contains a particular key.
     *
     * @param key the key to check for.
     * @return true if the key is present, false otherwise.
     */
    public boolean containsKey(String key) {
        return map.containsKey(key);
    }

    /**
     * Returns the value of a particular key (or null of the key doesn't exist).
     *
     * @param key the key to retrieve the value of.
     * @return the value or null.
     */
    public String get(String key) {
        return map.get(key);
    }

    /**
     * Returns the value of a particular key (or null of the key doesn't exist). Should the value contain comma (,)
     * separators, it is split into an array of values. Each array item is trimmed using {@link String#trim()}.
     *
     * @param key the key to retrieve the value of.
     * @return the split value or null.
     */
    public String[] getArray(String key) {
        String s = map.get(key);
        if (s == null) {
            return null;
        } else {
            return Arrays.stream(s.split(",")).map(String::trim).toArray(String[]::new);
        }
    }

    /**
     * Returns an unmodifiable set of all the keys.
     *
     * @return the unmodifiable set.
     */
    public Set<String> keySet() {
        return Collections.unmodifiableSet(map.keySet());
    }

    /**
     * Returns an unmodifiable collection of all the values.
     *
     * @return the unmodifiable collection.
     */
    public Collection<String> values() {
        return Collections.unmodifiableCollection(map.values());
    }

    /**
     * Returns an unmodifiable set of the key/value pairs as map entries.
     *
     * @return the unmodifiable set.
     */
    public Set<Map.Entry<String, String>> entrySet() {
        return Collections.unmodifiableSet(map.entrySet());
    }

    /**
     * Returns the value of a particular key (or the default value of the key doesn't exist).
     *
     * @param key          the key to retrieve the value of.
     * @param defaultValue the default value.
     * @return the value or the default value.
     */
    public String getOrDefault(String key, String defaultValue) {
        return map.getOrDefault(key, defaultValue);
    }

    /**
     * Walks the key/value pairs and provides them to the specified {@link BiConsumer}.
     *
     * @param action the {@link BiConsumer} that is invoked with each key/value pair.
     */
    public void forEach(BiConsumer<? super String, ? super String> action) {
        map.forEach(action);
    }

    /**
     * Returns the class configuration as an unmodifiable map.
     *
     * @return the unmodifiable map.
     */
    public Map<String, String> asMap() {
        return Collections.unmodifiableMap(map);
    }

    /**
     * Merge the class configuration with another one, overriding the existing values having an identical key. If a key
     * with a null value is merged, the key is completely removed from the resulting configuration.
     *
     * @param other the other class configuration.
     * @return the merge class configuration.
     */
    public ClassConfiguration<T> merge(ClassConfiguration<T> other) {
        if (!targetClass.isAssignableFrom(other.targetClass)) {
            throw new IllegalArgumentException(
                    "Cannot merge class configurations: " + targetClass.getName() + " is not assignable to " + other
                            .targetClass.getName());
        }
        map.putAll(other.map);
        map.values().removeIf(Objects::isNull);
        return this;
    }

    public Class<T> getTargetClass() {
        return targetClass;
    }
}
