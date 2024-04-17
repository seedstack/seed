/*
 * Copyright © 2013-2024, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.seedstack.coffig.Coffig;
import org.seedstack.coffig.TreeNode;
import org.seedstack.coffig.node.MapNode;
import org.seedstack.coffig.node.NamedNode;
import org.seedstack.coffig.node.ValueNode;
import org.seedstack.coffig.provider.JacksonProvider;
import org.seedstack.coffig.spi.ConfigurationProcessor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonConfigProcessor implements ConfigurationProcessor {
    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    private Coffig coffig;

    @Override
    public void initialize(Coffig coffig) {
        this.coffig = coffig;
    }

    @Override
    public void process(MapNode configuration) {
        Map<MapNode, List<String>> toParse = new HashMap<>();

        configuration.walk()
                .filter(node -> node instanceof MapNode)
                .map(MapNode.class::cast)
                .forEach(mapNode -> mapNode.namedNodes()
                        .filter(this::isJsonOrYamlFormatted)
                        .forEach(namedNode -> toParse.computeIfAbsent(mapNode, k -> new ArrayList<>())
                                .add(namedNode.name()))
                );

        // Replace the value with the parsed JSON/YAML
        toParse.forEach((parent, list) -> list.forEach(key -> {
            String newKey = key.substring(0, key.length() - 5);
            String rawValue = (String) coffig.getMapper().map(parent.remove(key), String.class);

            try {
                parent.set(newKey, JacksonProvider.buildTreeFromString(yamlMapper, rawValue));
            } catch (Exception e) {
                parent.set(newKey, new ValueNode(TreeNode.formatNodeError(e)));
            }
        }));
    }

    private boolean isJsonOrYamlFormatted(NamedNode namedNode) {
        return namedNode.name().endsWith("|json") || namedNode.name().endsWith("|yaml");
    }
}
