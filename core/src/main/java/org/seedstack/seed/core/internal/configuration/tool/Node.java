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
import org.seedstack.coffig.util.Utils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.SortedMap;
import java.util.TreeMap;

class Node implements Comparable<Node> {
    private final String name;
    private final SortedMap<String, Node> children = new TreeMap<>();
    private Class<?> configClass;
    private Class<?> outermostClass;
    private String[] path;
    private PropertyInfo[] propertyInfo;

    Node(Class<?> configClass) {
        this.configClass = configClass;

        List<String> path = new ArrayList<>();
        Class<?> previousClass = configClass;
        do {
            Config annotation = configClass.getAnnotation(Config.class);
            if (annotation == null) {
                break;
            }
            List<String> splitPath = Arrays.asList(annotation.value().split("\\."));
            Collections.reverse(splitPath);
            path.addAll(splitPath);
            previousClass = configClass;
        } while ((configClass = configClass.getDeclaringClass()) != null);

        Collections.reverse(path);
        this.outermostClass = previousClass;
        this.path = path.toArray(new String[path.size()]);
        this.name = this.path[this.path.length - 1];
        List<PropertyInfo> propertyInfo = buildPropertyInfo();
        this.propertyInfo = propertyInfo.toArray(new PropertyInfo[propertyInfo.size()]);
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

    public PropertyInfo[] getPropertyInfo() {
        return propertyInfo;
    }

    public String toString() {
        return String.join(".", (CharSequence[]) path);
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

    private List<PropertyInfo> buildPropertyInfo() {
        List<PropertyInfo> result = new ArrayList<>();
        ResourceBundle bundle = null;
        try {
            bundle = ResourceBundle.getBundle(outermostClass.getName());
        } catch (MissingResourceException e) {
            // ignore
        }

        for (Field field : configClass.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            if (field.getType().isAnnotationPresent(Config.class)) {
                continue;
            }
            PropertyInfo propertyInfo = new PropertyInfo();
            Config configAnnotation = field.getAnnotation(Config.class);
            String name;
            if (configAnnotation != null) {
                name = configAnnotation.value();
            } else {
                name = field.getName();
            }

            propertyInfo.setName(name);
            propertyInfo.setShortDescription(" " + getMessage(bundle, "", buildKey(name)));
            propertyInfo.setLongDescription(getMessage(bundle, propertyInfo.getShortDescription(), buildKey(name, "long")));
            propertyInfo.setType(Utils.getSimpleTypeName(field.getGenericType()));
            propertyInfo.setSingleValue(field.isAnnotationPresent(SingleValue.class));

            result.add(propertyInfo);
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
            for (int i = 1; i < path.length; i++) {
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
