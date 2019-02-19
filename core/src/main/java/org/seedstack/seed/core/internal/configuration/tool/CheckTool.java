/*
 * Copyright © 2013-2021, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.configuration.tool;

import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.api.plugin.request.ClasspathScanRequest;
import org.seedstack.coffig.Config;
import org.seedstack.coffig.TreeNode;
import org.seedstack.seed.Configuration;
import org.seedstack.seed.cli.CliArgs;
import org.seedstack.seed.cli.CliOption;
import org.seedstack.seed.core.internal.configuration.tool.utils.ConfigurationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.seedstack.seed.core.internal.configuration.tool.utils.ClassUtils.*;

public class CheckTool extends AbstractConfigTool {
    private static final Logger LOGGER = LoggerFactory.getLogger(CheckTool.class);

    private static final String PATH_SEPARATOR_REGEXP = "\\.";
    // List of classes that use @Configuration annotation with a specific property
    private final Map<String, List<Class>> annotationValues = new HashMap<>();

    @CliArgs
    private String[] args;
    @CliOption(name = "v", longName = "verbose")
    private boolean verbose;

    // root entries to ignore
    private List<String> exclusionList = new ArrayList<>(Arrays.asList("env", "sys", "runtime", "classes"));

    // Result of analysis. Next evolution: build an extended Coffig class where nodes will contains analysis result information
    private List<EntrySearchResult> results = new ArrayList<>();

    // Default minimum severity to display. Can be set to INFO using verbose mode.
    private SeverityEnum severity = SeverityEnum.WARNING;

    @Override
    public String toolName() {
        return "check";
    }

    /**
     * Need to find classes annotated with @Config (parent behaviour), plus classes containing fields annotated with @Configuration
     * {@inheritDoc}
     */
    @Override
    public Collection<ClasspathScanRequest> classpathScanRequests() {
        return classpathScanRequestBuilder()
                .annotationType(Config.class)
                .predicate(SeedConfigurationAnnotationPredicate.INSTANCE)
                .build();
    }

    @Override
    protected InitState initialize(InitContext initContext) {
        initContext.scannedTypesByPredicate()
                .get(SeedConfigurationAnnotationPredicate.INSTANCE)
                .forEach(myClass -> {
                            // Find list of Configuration-annotated fields
                            annotationValues.putAll(ConfigurationUtils.extractConfigurationAnnotatedFields(myClass));
                        }
                );

        return super.initialize(initContext);
    }

    @Override
    public Integer call() throws Exception {
        // Get extra parameters
        if (verbose) {
            severity = SeverityEnum.INFO;
        }

        // browse the Coffig tree to check if entries are bound to annotations in framework or application code
        TreeNode coffigTree = configuration.getTree();
        LOGGER.debug("Analysis start");
        walk(coffigTree, "", null);
        LOGGER.debug("Analysis end");

        // Display usage
        usage();

        // Display results
        results.stream().filter(result -> result.severity.compareTo(severity) >= 0).sorted(Comparator
                .comparing(EntrySearchResult::getSeverity)
                .thenComparing(EntrySearchResult::getStatus)
                .thenComparing(EntrySearchResult::getPath)).forEach(System.out::println);
        return 0;
    }

    private void usage() {
        System.out.println("This tool reports configuration properties without associated class or field in code.");
        System.out.println("Known limitations:");
        System.out.println("  - In verbose mode, properties associated with multiple classes/fields will be reported with only one class/field");
        System.out.println("  - The tool will not recurse into arrays of maps (but arrays of user objects will be analyzed correctly).");
    }

    /**
     * @param coffigTree  Tree to walk through
     * @param path        Path to parent tree
     * @param parentClass Class associated with parent tree node, if any
     */
    private void walk(TreeNode coffigTree, String path, Class parentClass) {
        if (coffigTree.type().equals(TreeNode.Type.MAP_NODE)) {

            coffigTree.namedNodes()
                    .forEach(nn -> {
                        LOGGER.debug("Processing {}", path + nn.name());
                        String fullPath = path + nn.name();
                        // do not analyze excluded properties
                        if (exclusionList.contains(fullPath)) {
                            LOGGER.debug(new EntrySearchResult(SeverityEnum.DEBUG, fullPath, StatusEnum.SKIPPED).toString());
                            return;
                        }

                        /** {@link root} contains tree of Nodes for @Config-annotated classes
                         currentNode will be used to extract associated class from root, if any
                         */
                        Node currentNode = root.find(fullPath.split(PATH_SEPARATOR_REGEXP));
                        // Case of a leaf
                        if (nn.node().type() == TreeNode.Type.VALUE_NODE) {
                            if (currentNode != null) {
                                // We have found a class in root, we add it to the results.
                                results.add(new EntrySearchResult(SeverityEnum.INFO, fullPath, StatusEnum.FOUND, currentNode.getConfigClass()));
                            } else if (getField(parentClass, nn.name()).isPresent()) {
                                // Or, there is an @Config field with such a name in parent class
                                results.add(new EntrySearchResult(SeverityEnum.INFO, fullPath, StatusEnum.FOUND, parentClass));
                            } else if (annotationValues.containsKey(fullPath)) {
                                // Or, there is an @Configuration field in parent class
                                results.add(new EntrySearchResult(SeverityEnum.INFO, fullPath, StatusEnum.FOUND, annotationValues.get(fullPath)));
                            } else {
                                results.add(new EntrySearchResult(SeverityEnum.WARNING, fullPath, StatusEnum.NOT_FOUND));
                            }
                            return;
                        }
                        // Case of a node that is not a leaf

                        // There is matching entry in root, we keep walking
                        if (currentNode != null) {
                            Class associatedClass = currentNode.getConfigClass();
                            walk(nn.node(), path + nn.name() + ".", associatedClass);
                            return;
                        }

                        // There is no entry in root
                        if (parentClass == null) {
                            // There is no associated class in root, we look into @Configuration fields for fullPath
                            if (annotationValues.containsKey(fullPath)) {
                                // There is an @Configuration-field in parent class. We consider first entry as the new parentClass
                                Optional<Field> optionalField = getField(annotationValues.get(fullPath).get(0), nn.name(), Configuration.class);
                                Class associatedClass = getDeclaringClass(optionalField.get());
                                if (isPrimitive(associatedClass)) {
                                    // Primitive-type class are considered as nodes
                                    results.add(new EntrySearchResult(SeverityEnum.INFO, fullPath, StatusEnum.FOUND, annotationValues.get(fullPath)));
                                } else {
                                    // Non-primitive class: keep walking
                                    walk(nn.node(), path + nn.name() + ".", associatedClass);
                                }
                            // we look for an Configuration-annotated field whose value starts with fullPath
                            } else if (annotationValues.keySet().stream().anyMatch(n -> n.startsWith(fullPath+ "."))) {
                                walk(nn.node(), path + nn.name() + ".", null);
                            }
                            else {
                                results.add(new EntrySearchResult(SeverityEnum.WARNING, fullPath, StatusEnum.NOT_FOUND));
                            }
                        } else {
                            // There is a parent class
                            Optional<Field> optionalField = getField(parentClass, nn.name());
                            if (optionalField.isPresent()) {
                                // Field is declared in class
                                Class associatedClass = getDeclaringClass(optionalField.get());
                                if (isPrimitive(associatedClass)) {
                                    // Primitive-type class are considered as nodes
                                    results.add(new EntrySearchResult(SeverityEnum.INFO, fullPath, StatusEnum.FOUND, parentClass));
                                } else {
                                    // Non-primitive class: keep walking
                                    walk(nn.node(), path + nn.name() + ".", associatedClass);
                                }
                            } else {
                                // no field associated: node may be associated with a map entry
                                walk(nn.node(), path + nn.name() + ".", parentClass);
                            }
                        }
                    });
        } else if (coffigTree.type().equals(TreeNode.Type.ARRAY_NODE)) {
            // Happens only when array contains non primitive objects
            coffigTree.nodes().filter(nn -> nn.type().equals(TreeNode.Type.MAP_NODE)).forEach(nn -> {
                walk(nn, path, parentClass);
            });
        } else {
            LOGGER.warn("Method called with an expected TreeNode type");
        }

    }

    /**
     * Severity to associate with configuration analysis results
     */
    public enum SeverityEnum {
        DEBUG, INFO, WARNING, ERROR;

        public static String toList() {
            return Arrays.stream(SeverityEnum.values()).
                    sorted().
                    map(SeverityEnum::toString).
                    collect(Collectors.joining(","));
        }
    }

    enum StatusEnum {
        SKIPPED("property '%s' not analyzed"),
        FOUND("property '%s' associated with '%s'"),
        NOT_FOUND("no usage found for property '%s'");

        private String pattern;

        private StatusEnum(String pattern) {
            this.pattern = pattern;
        }

        public String getPattern() {
            return pattern;
        }
    }

    public static class EntrySearchResult {

        /**
         * Associated check type ("config" only first)
         */
        private String type = "config";
        /**
         * Message severity
         */
        private SeverityEnum severity;
        /**
         * property path in configuration files
         */
        private String path;
        /**
         * Status used for configuration checks
         */
        private StatusEnum status;
        /**
         * List of classes containing attributes matching configuration properties
         */
        private String[] classNames = {};

        public EntrySearchResult(SeverityEnum severity, String path, StatusEnum status) {
            this.severity = severity;
            this.path = path;
            this.status = status;
        }

        public EntrySearchResult(SeverityEnum severity, String path, StatusEnum status, Class configClass) {
            this.severity = severity;
            this.path = path;
            this.status = status;
            this.classNames = new String[]{configClass.getName()};
        }

        public EntrySearchResult(SeverityEnum severity, String path, StatusEnum status, List<Class> classList) {
            this.severity = severity;
            this.path = path;
            this.status = status;
            this.classNames = classList.stream().map(Class::getName).toArray(size -> new String[size]);
        }

        public SeverityEnum getSeverity() {
            return severity;
        }

        public String toString() {
            String pattern = status.getPattern();
            String message = "";
            switch (status) {
                case FOUND:
                    String classList = Arrays.stream(classNames).distinct().sorted().collect(Collectors.joining(","));
                    if (classNames.length > 1) {
                        classList = "[" + classList + "]";
                    }
                    message = String.format(pattern, path, classList);
                    break;
                case NOT_FOUND:
                case SKIPPED:
                    message = String.format(pattern, path);
            }
            return String.format("%s %-10s %s", severity, type, message);
        }

        public String getPath() {
            return path;
        }

        public StatusEnum getStatus() {
            return status;
        }
    }
}
