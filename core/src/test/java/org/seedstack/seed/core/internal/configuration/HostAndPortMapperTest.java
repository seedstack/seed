/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.internal.configuration;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.google.common.net.HostAndPort;
import org.junit.Test;
import org.seedstack.coffig.Coffig;
import org.seedstack.coffig.node.ValueNode;
import org.seedstack.coffig.spi.ConfigurationMapper;

public class HostAndPortMapperTest {
    private static final String SOME_HOST_4576 = "someHost:4576";
    private ConfigurationMapper mapper = Coffig.basic().getMapper();

    @Test
    public void testMapHostAndPort() {
        assertThat((HostAndPort) (mapper.map(new ValueNode(SOME_HOST_4576), HostAndPort.class))).isEqualTo(
                HostAndPort.fromString(SOME_HOST_4576));
    }

    @Test
    public void testUnmapHostAndPort() {
        assertThat(mapper.unmap(HostAndPort.fromString(SOME_HOST_4576), HostAndPort.class)).isEqualTo(
                new ValueNode(SOME_HOST_4576));
    }
}
