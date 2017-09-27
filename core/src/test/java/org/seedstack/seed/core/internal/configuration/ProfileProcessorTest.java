/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
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

public class ProfileProcessorTest {
    private ProfileProcessor profileProcessor = new ProfileProcessor();
    private MapNode config;

    @Before
    public void setUp() throws Exception {
        config = new MapNode(
                new NamedNode("a", "1"),
                new NamedNode("b<profile1>", "2"),
                new NamedNode("c<profile2>", new MapNode(
                        new NamedNode("ca", "3"),
                        new NamedNode("cb<profile3>", "4"),
                        new NamedNode("cc<profile1, profile3>", "5")
                ))
        );
    }

    @Test
    public void testNoProfile() throws Exception {
        System.clearProperty("seedstack.profiles");
        profileProcessor.process(config);
        assertThat(config.get("a").get()).isEqualTo(new ValueNode("1"));
        assertThat(config.get("b").isPresent()).isFalse();
        assertThat(config.get("c").isPresent()).isFalse();
    }

    @Test
    public void testProfile1() throws Exception {
        System.setProperty("seedstack.profiles", "profile1");
        profileProcessor.process(config);
        assertThat(config.get("a").get()).isEqualTo(new ValueNode("1"));
        assertThat(config.get("b").get()).isEqualTo(new ValueNode("2"));
        assertThat(config.get("c").isPresent()).isFalse();
        System.clearProperty("seedstack.profiles");
    }

    @Test
    public void testProfile2() throws Exception {
        System.setProperty("seedstack.profiles", "profile2");
        profileProcessor.process(config);
        assertThat(config.get("a").get()).isEqualTo(new ValueNode("1"));
        assertThat(config.get("b").isPresent()).isFalse();
        assertThat(config.get("c").isPresent()).isTrue();
        assertThat(config.get("c.ca").get()).isEqualTo(new ValueNode("3"));
        assertThat(config.get("c.cb").isPresent()).isFalse();
        assertThat(config.get("c.cc").isPresent()).isFalse();
        System.clearProperty("seedstack.profiles");
    }

    @Test
    public void testProfile3() throws Exception {
        System.setProperty("seedstack.profiles", "profile3");
        profileProcessor.process(config);
        assertThat(config.get("a").get()).isEqualTo(new ValueNode("1"));
        assertThat(config.get("b").isPresent()).isFalse();
        assertThat(config.get("c").isPresent()).isFalse();
        System.clearProperty("seedstack.profiles");
    }

    @Test
    public void testProfile2And3() throws Exception {
        System.setProperty("seedstack.profiles", "profile2, profile3");
        profileProcessor.process(config);
        assertThat(config.get("a").get()).isEqualTo(new ValueNode("1"));
        assertThat(config.get("b").isPresent()).isFalse();
        assertThat(config.get("c").isPresent()).isTrue();
        assertThat(config.get("c.ca").get()).isEqualTo(new ValueNode("3"));
        assertThat(config.get("c.cb").get()).isEqualTo(new ValueNode("4"));
        assertThat(config.get("c.cc").get()).isEqualTo(new ValueNode("5"));
        System.clearProperty("seedstack.profiles");
    }

    @Test
    public void testProfile1And2() throws Exception {
        System.setProperty("seedstack.profiles", "profile1, profile2");
        profileProcessor.process(config);
        assertThat(config.get("a").get()).isEqualTo(new ValueNode("1"));
        assertThat(config.get("b").get()).isEqualTo(new ValueNode("2"));
        assertThat(config.get("c").isPresent()).isTrue();
        assertThat(config.get("c.ca").get()).isEqualTo(new ValueNode("3"));
        assertThat(config.get("c.cb").isPresent()).isFalse();
        assertThat(config.get("c.cc").get()).isEqualTo(new ValueNode("5"));
        System.clearProperty("seedstack.profiles");
    }
}
