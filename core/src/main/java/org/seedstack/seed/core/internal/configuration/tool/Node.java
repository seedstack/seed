/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.configuration.tool;

import org.seedstack.coffig.Config;
import org.seedstack.coffig.SingleValue;
import org.seedstack.shed.reflect.Annotations;

import javax.validation.constraints.NotNull;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.SortedMap;
import java.util.TreeMap;

import static org.seedstack.shed.reflect.Classes.instantiateDefault;
import static org.seedstack.shed.reflect.ReflectUtils.makeAccessible;
import static org.seedstack.shed.reflect.Types.simpleNameOf;

class Node implements Comparable<Node> {
    private final String name;
    private final Class<?> configClass;
    private final Class<?> outermostClass;
    private final int outermostLevel;
    private final String[] path;
    private final Map<String, PropertyInfo> propertyInfo;
    private final SortedMap<String, Node> children = new TreeMap<>();

    Node() {
        this.name = "";
        this.configClass = null;
        this.outermostClass = null;
        this.outermostLevel = 0;
        this.path = new String[0];
        this.propertyInfo = new HashMap<>();
    }

    Node(Class<?> configClass) {
        this.configClass = configClass;

        List<String> path = new ArrayList<>();
        Class<?> previousClass = configClass;
        int nestingLevel = -1;
        do {
            Config annotation = configClass.getAnnotation(Config.class);
            if (annotation == null) {
                break;
            }
            List<String> splitPath = Arrays.asList(annotation.value().split("\\."));
            Collections.reverse(splitPath);
            path.addAll(splitPath);
            previousClass = configClass;
            nestingLevel++;
        } while ((configClass = configClass.getDeclaringClass()) != null);

        Collections.reverse(path);
        this.outermostClass = previousClass;
        this.outermostLevel = this.outermostClass.getAnnotation(Config.class).value().split("\\.").length;
        this.path = path.toArray(new String[path.size()]);
        this.name = this.path[this.path.length - 1];
        this.propertyInfo = buildPropertyInfo();
    }

    String getName() {
        return name;
    }

    String[] getPath() {
        return path;
    }

    Node getChild(String name) {
        return children.get(name);
    }

    void addChild(Node node) {
        children.put(node.getName(), node);
    }

    Collection<Node> getChildren() {
        return Collections.unmodifiableCollection(children.values());
    }

    Collection<PropertyInfo> getPropertyInfo() {
        return propertyInfo.values();
    }

    PropertyInfo getPropertyInfo(String name) {
        return propertyInfo.get(name);
    }

    boolean isRootNode() {
        return configClass == null;
    }

    public String toString() {
        return String.join(".", (CharSequence[]) path);
    }

    Node find(String... path) {
        if (path.length == 0) {
            return this;
        }
        for (Node node : children.values()) {
            if (path[0].equals(node.name)) {
                return node.find(Arrays.copyOfRange(path, 1, path.length));
            }
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Node node = (Node) o;

        return o.toString().equals(node.toString());

    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public int compareTo(Node that) {
        if (this.toString().compareTo(that.toString()) < 0) {
            return -1;
        } else if (this.toString().compareTo(that.toString()) > 0) {
            return 1;
        }
        return 0;
    }

    private Map<String, PropertyInfo> buildPropertyInfo() {
        Map<String, PropertyInfo> result = new HashMap<>();

        ResourceBundle bundle = null;
        try {
            bundle = ResourceBundle.getBundle(outermostClass.getName());
        } catch (MissingResourceException e) {
            // ignore
        }

        Object defaultInstance;
        try {
            defaultInstance = instantiateDefault(configClass);
        } catch (Exception e) {
            defaultInstance = null;
        }

        for (Field field : configClass.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            if (field.getType().isAnnotationPresent(Config.class)) {
                continue;
            }

            makeAccessible(field);

            PropertyInfo propertyInfo = new PropertyInfo();
            Config configAnnotation = field.getAnnotation(Config.class);
            String name;
            if (configAnnotation != null) {
                name = configAnnotation.value();
            } else {
                name = field.getName();
            }

            propertyInfo.setName(name);
            propertyInfo.setShortDescription(getMessage(bundle, "No description.", buildKey(name)));
            propertyInfo.setLongDescription(getMessage(bundle, null, buildKey(name, "long")));
            propertyInfo.setType(simpleNameOf(field.getGenericType()));
            propertyInfo.setSingleValue(field.isAnnotationPresent(SingleValue.class));
            if (defaultInstance != null) {
                try {
                    propertyInfo.setDefaultValue(field.get(defaultInstance));
                } catch (IllegalAccessException e) {
                    // ignore
                }
            }
            propertyInfo.setMandatory(propertyInfo.getDefaultValue() == null && Annotations.on(field).includingMetaAnnotations().find(NotNull.class).isPresent());

            result.put(name, propertyInfo);
        }

        return result;
    }

    private String getMessage(ResourceBundle resourceBundle, String defaultMessage, String key) {
        if (resourceBundle == null) {
            return defaultMessage;
        }
        try {
            return resourceBundle.getString(key);
        } catch (MissingResourceException e) {
            return defaultMessage;
        }
    }

    private String buildKey(String... parts) {
        StringBuilder sb = new StringBuilder();
        if (!configClass.equals(outermostClass)) {
            for (int i = outermostLevel; i < path.length; i++) {
                sb.append(path[i]);
                if (i < path.length - 1 || parts.length > 0) {
                    sb.append(".");
                }
            }
        }
        for (int i = 0; i < parts.length; i++) {
            sb.append(parts[i]);
            if (i < parts.length - 1) {
                sb.append(".");
            }
        }
        return sb.toString();
    }
}
