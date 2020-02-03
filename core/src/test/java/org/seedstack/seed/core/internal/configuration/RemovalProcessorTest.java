/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.seedstack.coffig.node.MapNode;
import org.seedstack.coffig.node.NamedNode;
import org.seedstack.coffig.node.ValueNode;

public class RemovalProcessorTest {
    private RemovalProcessor removalProcessor = new RemovalProcessor();
    private MapNode config;

    @Before
    public void setUp() {
        config = new MapNode(
                new NamedNode("a", "1"),
                new NamedNode("b", "2"),
                new NamedNode("-b", "2bis"),

                new NamedNode("c", new MapNode(
                        new NamedNode("aa", "3"),
                        new NamedNode("bb", "4"),
                        new NamedNode("-bb", "4bis")
                )),

                new NamedNode("d", new MapNode(
                        new NamedNode("aa", "5"),
                        new NamedNode("bb", "6")
                )),

                new NamedNode("-d", new ValueNode(""))
        );
        removalProcessor.process(config);
    }

    @Test
    public void testRemoval() {
        assertThat(config.get("a").get().value()).isEqualTo("1");
        assertThat(config.get("b").isPresent()).isFalse();
        assertThat(config.get("-b").isPresent()).isFalse();
        assertThat(config.get("c.aa").get().value()).isEqualTo("3");
        assertThat(config.get("c.bb").isPresent()).isFalse();
        assertThat(config.get("c.-bb").isPresent()).isFalse();
        assertThat(config.get("d").isPresent()).isFalse();
        assertThat(config.get("-d").isPresent()).isFalse();
    }
}
