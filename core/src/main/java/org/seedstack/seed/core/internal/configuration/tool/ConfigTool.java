/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.configuration.tool;

import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.api.plugin.request.ClasspathScanRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.seedstack.coffig.Coffig;
import org.seedstack.coffig.Config;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.cli.CliArgs;
import org.seedstack.seed.core.SeedRuntime;
import org.seedstack.seed.core.internal.AbstractSeedTool;
import org.seedstack.seed.core.internal.CoreErrorCode;

public class ConfigTool extends AbstractSeedTool {
    private final Node root = new Node();
    @CliArgs
    private String[] args;
    private Coffig configuration;

    @Override
    public String toolName() {
        return "config";
    }

    @Override
    protected void setup(SeedRuntime seedRuntime) {
        configuration = seedRuntime.getConfiguration();
    }

    @Override
    public Collection<ClasspathScanRequest> classpathScanRequests() {
        return classpathScanRequestBuilder()
                .annotationType(Config.class)
                .build();
    }

    @Override
    protected InitState initialize(InitContext initContext) {
        List<Node> nodes = new ArrayList<>();
        initContext.scannedClassesByAnnotationClass().get(Config.class).stream().map(
                configClass -> new Node(configClass, configuration)).forEach(nodes::add);
        Collections.sort(nodes);
        nodes.forEach(this::buildTree);
        return InitState.INITIALIZED;
    }

    @Override
    public Integer call() throws Exception {
        if (args != null && args.length > 0) {
            String[] path = String.join(".", (CharSequence[]) args).split("\\.");
            Node node = root.find(path);
            if (node == null) {
                info(path);
            } else {
                new TreePrinter(node).printTree(System.out);
            }
        } else {
            new TreePrinter(root).printTree(System.out);
        }
        return 0;
    }

    private void info(String[] path) {
        Node node = root.find(Arrays.copyOfRange(path, 0, path.length - 1));
        if (node == null) {
            throw SeedException.createNew(CoreErrorCode.INVALID_CONFIG_PATH).put("path", String.join(".", path));
        } else {
            PropertyInfo propertyInfo = node.getPropertyInfo(path[path.length - 1]);
            if (propertyInfo == null) {
                throw SeedException.createNew(CoreErrorCode.INVALID_CONFIG_PROPERTY).put("property",
                        path[path.length - 1]);
            }
            new DetailPrinter(propertyInfo).printDetail(System.out);
        }
    }

    private void buildTree(Node node) {
        Node current = root;
        String[] path = node.getPath();
        for (String part : path) {
            if (!part.isEmpty()) {
                Node child = current.getChild(part);
                if (child != null) {
                    current = child;
                } else {
                    current.addChild(node);
                    break;
                }
            }
        }
    }
}
