/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.configuration;

import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.api.plugin.request.ClasspathScanRequest;
import org.fusesource.jansi.Ansi;
import org.seedstack.coffig.Config;
import org.seedstack.coffig.SingleValue;
import org.seedstack.coffig.util.Utils;
import org.seedstack.seed.CoreConfig;
import org.seedstack.seed.core.internal.AbstractSeedTool;

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

public class ConfigurationTool extends AbstractSeedTool {
    private static final String INDENTATION = "  ";
    private Node root = new Node("", CoreConfig.class);

    @Override
    public String toolName() {
        return "config";
    }

    @Override
    public Collection<ClasspathScanRequest> classpathScanRequests() {
        return classpathScanRequestBuilder()
                .annotationType(Config.class)
                .build();
    }

    @Override
    protected InitState initialize(InitContext initContext) {
        initContext.scannedClassesByAnnotationClass().get(Config.class).forEach(this::buildTree);
        return InitState.INITIALIZED;
    }

    @Override
    public Integer call() throws Exception {
        Ansi ansi = Ansi.ansi();
        printTree(root, "", ansi);
        System.out.println(ansi.toString());
        return 0;
    }

    private void printTree(Node node, String leftPadding, Ansi ansi) {
        ansi
                .a(leftPadding)
                .fg(Ansi.Color.YELLOW).a(node.name.isEmpty() ? "<ROOT>" : node.name).reset()
                .newline();

        for (PropertyInfo propertyInfo : buildPropertyInfo(node.configClass)) {
            ansi
                    .a(leftPadding).a("  ")
                    .fgBright(Ansi.Color.CYAN).a(propertyInfo.singleValue ? "+" : "").a(propertyInfo.name).reset()
                    .a("(")
                    .fgBright(Ansi.Color.MAGENTA).a(propertyInfo.type).reset()
                    .a(")")
                    .a(propertyInfo.shortDescription)
                    .newline();
        }

        for (Node child : node.children.values()) {
            printTree(child, leftPadding + INDENTATION, ansi);
        }
    }

    private List<PropertyInfo> buildPropertyInfo(Class<?> configClass) {
        List<PropertyInfo> result = new ArrayList<>();
        ResourceBundle bundle = null;
        try {
            bundle = ResourceBundle.getBundle(configClass.getCanonicalName());
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
            String name = configAnnotation != null ? configAnnotation.value() : field.getName();

            propertyInfo.name = name;
            propertyInfo.shortDescription = " " + getMessage(bundle, "", name);
            propertyInfo.longDescription = getMessage(bundle, propertyInfo.shortDescription, name, "long");
            propertyInfo.type = Utils.getSimpleTypeName(field.getGenericType());
            propertyInfo.singleValue = field.isAnnotationPresent(SingleValue.class);

            result.add(propertyInfo);
        }

        return result;
    }

    private String getMessage(ResourceBundle resourceBundle, String defaultMessage, String... key) {
        if (resourceBundle == null) {
            return defaultMessage;
        }
        if (!resourceBundle.containsKey(String.join(".", (CharSequence[]) key))) {
            return defaultMessage;
        }
        return resourceBundle.getString(String.join(".", (CharSequence[]) key));
    }

    private void buildTree(Class<?> aClass) {
        String[] fullPath = getPath(aClass);
        Node current = root;
        for (int i = 0; i < fullPath.length; i++) {
            String part = fullPath[i];
            if (!part.isEmpty()) {
                Node child = current.children.get(part);
                if (child != null) {
                    current = child;
                } else {
                    current.children.put(part, current = new Node(part));
                }
                if (i == fullPath.length - 1) {
                    current.configClass = aClass;
                }
            }
        }
    }

    private String[] getPath(Class<?> configClass) {
        List<String> path = new ArrayList<>();
        do {
            Config annotation = configClass.getAnnotation(Config.class);
            if (annotation == null) {
                break;
            }
            List<String> splitPath = Arrays.asList(annotation.value().split("\\."));
            Collections.reverse(splitPath);
            path.addAll(splitPath);
        } while ((configClass = configClass.getDeclaringClass()) != null);
        Collections.reverse(path);
        return path.toArray(new String[path.size()]);
    }

    private static class Node implements Comparable<Node> {
        private final String name;
        private final SortedMap<String, Node> children = new TreeMap<>();
        private Class<?> configClass;

        private Node(String name) {
            this.name = name;
        }

        private Node(String name, Class<?> configClass) {
            this.name = name;
            this.configClass = configClass;
        }

        @Override
        public int compareTo(Node o) {
            return name.compareTo(o.name);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Node node = (Node) o;

            return name.equals(node.name);

        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }
    }

    private static class PropertyInfo {
        private String name;
        private String type;
        private String shortDescription;
        private String longDescription;
        private boolean singleValue;
    }
}
