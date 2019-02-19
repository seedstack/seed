/*
 * Copyright © 2013-2021, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.configuration.tool;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.api.plugin.request.ClasspathScanRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.seedstack.coffig.Coffig;
import org.seedstack.coffig.Config;
import org.seedstack.seed.core.SeedRuntime;
import org.seedstack.seed.core.internal.AbstractSeedTool;

public abstract class AbstractConfigTool extends AbstractSeedTool {
    final Node root = new Node();
    @SuppressFBWarnings(value = "UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR", justification = "Field is initialized by a call to 'setup' method")
    Coffig configuration;

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
