/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.configuration;

import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.seedstack.coffig.TreeNode;
import org.seedstack.coffig.node.MapNode;
import org.seedstack.coffig.spi.ConfigurationProcessor;

public class ProfileProcessor implements ConfigurationProcessor {
    private static final String SEEDSTACK_PROFILES_PROPERTY = "seedstack.profiles";
    private static Pattern keyWithProfilePattern = Pattern.compile("(.*)<([,\\s\\w]+)>");
    private static Predicate<String> notNullOrEmpty = ((Predicate<String>) Strings::isNullOrEmpty).negate();

    @Override
    public void process(MapNode configuration) {
        Set<String> activeProfiles = activeProfiles();
        Map<MapNode, List<String>> toRemove = new HashMap<>();
        Map<TreeNode, Map<String, String>> moves = new HashMap<>();

        configuration.walk()
                .filter(node -> node instanceof MapNode)
                .map(MapNode.class::cast)
                .forEach(mapNode -> mapNode.namedNodes().forEach(namedNode -> {
                    Matcher matcher = keyWithProfilePattern.matcher(namedNode.name());
                    if (matcher.matches()) {
                        if (parseProfiles(matcher.group(2)).stream().noneMatch(activeProfiles::contains)) {
                            toRemove.computeIfAbsent(mapNode, k -> new ArrayList<>()).add(namedNode.name());
                        } else {
                            moves.computeIfAbsent(mapNode, k -> new HashMap<>())
                                    .put(matcher.group(0), matcher.group(1));
                        }
                    }
                }));

        // Remove keys not in the active profiles
        toRemove.forEach((node, list) -> list.forEach(node::remove));

        // Remove the profiles from remaining keys containing them
        for (Map.Entry<TreeNode, Map<String, String>> movesEntry : moves.entrySet()) {
            for (Map.Entry<String, String> moveEntry : movesEntry.getValue().entrySet()) {
                movesEntry.getKey().move(moveEntry.getKey(), moveEntry.getValue());
            }
        }
    }

    private static Set<String> parseProfiles(String value) {
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(notNullOrEmpty)
                .collect(Collectors.toSet());
    }

    static Set<String> activeProfiles() {
        return parseProfiles(System.getProperty(SEEDSTACK_PROFILES_PROPERTY, ""));
    }
}
