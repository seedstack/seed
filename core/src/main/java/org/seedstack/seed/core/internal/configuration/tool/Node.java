/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.configuration.tool;

import static org.seedstack.shed.reflect.Classes.instantiateDefault;
import static org.seedstack.shed.reflect.ReflectUtils.makeAccessible;
import static org.seedstack.shed.reflect.Types.simpleNameOf;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.TreeMap;
import javax.validation.constraints.NotNull;
import org.seedstack.coffig.Coffig;
import org.seedstack.coffig.Config;
import org.seedstack.coffig.SingleValue;
import org.seedstack.shed.reflect.Annotations;
import org.seedstack.shed.reflect.ReflectUtils;
import org.seedstack.shed.reflect.Types;

class Node implements Comparable<Node> {
    private final Class<?> configClass;
    private final Coffig coffig;
    private final String name;
    private final Class<?> outermostClass;
    private final int outermostLevel;
    private final String[] path;
    private final ResourceBundle bundle;
    private final Map<String, PropertyInfo> propertyInfo;
    private final Map<String, Node> children = new TreeMap<>();

    Node() {
        this.configClass = null;
        this.coffig = null;
        this.name = "";
        this.outermostClass = null;
        this.outermostLevel = 0;
        this.path = new String[0];
        this.bundle = null;
        this.propertyInfo = new HashMap<>();
    }

    Node(Class<?> configClass, Coffig coffig) {
        this.configClass = configClass;
        this.coffig = coffig;

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
        this.outermostLevel = this.outermostClass.getAnnotation(Config.class).value().split("\\.").length;
        this.path = path.toArray(new String[path.size()]);
        this.name = this.path[this.path.length - 1];
        this.bundle = getResourceBundle();
        this.propertyInfo = buildPropertyInfo(this.configClass, "", null);
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

    private Map<String, PropertyInfo> buildPropertyInfo(Class<?> configClass, String parentPropertyName,
            Object defaultInstance) {
        Map<String, PropertyInfo> result = new LinkedHashMap<>();

        if (defaultInstance == null) {
            try {
                defaultInstance = instantiateDefault(configClass);
            } catch (Exception e) {
                defaultInstance = null;
            }
        }

        for (Field field : configClass.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                // Skip static fields (not used for configuration)
                continue;
            }
            if (field.getType().isAnnotationPresent(Config.class)) {
                // Skip fields of type annotated with @Config as they are already detected
                continue;
            }

            makeAccessible(field);

            Config configAnnotation = field.getAnnotation(Config.class);
            Type genericType = field.getGenericType();
            String name;
            if (configAnnotation != null) {
                name = configAnnotation.value();
            } else {
                name = field.getName();
            }

            PropertyInfo propertyInfo = new PropertyInfo();
            propertyInfo.setName(name);
            propertyInfo.setShortDescription(getMessage("", buildKey(parentPropertyName, name)));
            propertyInfo.setLongDescription(getMessage(null, buildKey(parentPropertyName, name, "long")));
            propertyInfo.setType(simpleNameOf(genericType));
            propertyInfo.setSingleValue(field.isAnnotationPresent(SingleValue.class));
            propertyInfo.setMandatory(isNotNull(field));
            if (defaultInstance != null) {
                propertyInfo.setDefaultValue(ReflectUtils.getValue(field, defaultInstance));
            }

            Class<?> rawClass = Types.rawClassOf(genericType);
            Type itemType = null;
            if (Collection.class.isAssignableFrom(rawClass) && genericType instanceof ParameterizedType) {
                itemType = Types.rawClassOf(((ParameterizedType) genericType).getActualTypeArguments()[0]);
            } else if (Map.class.isAssignableFrom(rawClass) && genericType instanceof ParameterizedType) {
                itemType = Types.rawClassOf(((ParameterizedType) genericType).getActualTypeArguments()[1]);
            } else if (genericType instanceof Class<?> && ((Class<?>) genericType).isArray()) {
                itemType = ((Class<?>) genericType).getComponentType();
            }

            if (itemType != null && !coffig.getMapper().canHandle(itemType)) {
                propertyInfo.addInnerPropertyInfo(
                        buildPropertyInfo(
                                Types.rawClassOf(itemType),
                                parentPropertyName.isEmpty() ? name : parentPropertyName + "." + name,
                                defaultInstance != null && itemType.equals(genericType) ?
                                        ReflectUtils.getValue(field, defaultInstance)
                                        : null
                        )
                );
            }

            result.put(name, propertyInfo);
        }

        return result;
    }

    private boolean isNotNull(Field field) {
        return Annotations.on(field).includingMetaAnnotations().find(NotNull.class).isPresent();
    }

    private String getMessage(String defaultMessage, String key) {
        if (bundle == null) {
            return defaultMessage;
        }
        try {
            return bundle.getString(key);
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
            if (!parts[i].isEmpty()) {
                sb.append(parts[i]);
                if (i < parts.length - 1) {
                    sb.append(".");
                }
            }
        }
        return sb.toString();
    }

    private ResourceBundle getResourceBundle() {
        ResourceBundle bundle;
        try {
            bundle = ResourceBundle.getBundle(outermostClass.getName());
        } catch (MissingResourceException e) {
            bundle = null;
        }
        return bundle;
    }
}
