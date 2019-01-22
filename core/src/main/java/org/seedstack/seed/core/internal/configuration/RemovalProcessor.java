/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.seedstack.coffig.TreeNode;
import org.seedstack.coffig.node.MapNode;
import org.seedstack.coffig.spi.ConfigurationProcessor;

public class RemovalProcessor implements ConfigurationProcessor {
    @Override
    public void process(MapNode configuration) {
        Map<MapNode, List<String>> toRemove = new HashMap<>();

        configuration.walk()
                .filter(node -> node.type() == TreeNode.Type.MAP_NODE)
                .map(MapNode.class::cast)
                .forEach(mapNode -> mapNode
                        .namedNodes()
                        .filter(namedNode -> namedNode.name().startsWith("-"))
                        .forEach(namedNode -> {
                            List<String> strings = toRemove.get(mapNode);
                            if (strings == null) {
                                toRemove.put(mapNode, strings = new ArrayList<>());
                            }
                            strings.add(namedNode.name());
                            strings.add(namedNode.name().substring(1));
                        }));

        toRemove.forEach((node, list) -> list.forEach(node::remove));
    }
}
