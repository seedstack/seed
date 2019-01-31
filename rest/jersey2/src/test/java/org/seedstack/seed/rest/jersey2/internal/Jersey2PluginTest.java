/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.jersey2.internal;

import com.google.common.collect.Lists;
import io.nuun.kernel.api.plugin.context.InitContext;
import mockit.Expectations;
import mockit.Mocked;
import org.junit.Test;
import org.seedstack.seed.rest.RestConfig;
import org.seedstack.seed.rest.internal.RestPlugin;
import org.seedstack.seed.rest.spi.RestProvider;
import org.seedstack.seed.web.internal.WebPlugin;
import org.seedstack.seed.web.spi.SeedFilterPriority;

import static org.assertj.core.api.Assertions.assertThat;

public class Jersey2PluginTest {

    private Jersey2Plugin underTest = new Jersey2Plugin();
    @Mocked
    private InitContext initContext;
    @Mocked
    private RestPlugin restPlugin;
    @Mocked
    private RestProvider restProvider;
    @Mocked
    private WebPlugin webPlugin;
    @Mocked
    private RestConfig restConfig;

    @Test
    public void testInit() {
        new Expectations() {{
            initContext.dependency(RestPlugin.class);
            result = restPlugin;

            restPlugin.isEnabled();
            result = true;

            restPlugin.getRestConfig();
            result = restConfig;

            initContext.dependencies(RestProvider.class);
            result = Lists.newArrayList(restProvider);
        }};

        underTest.init(initContext);
    }

    @Test
    public void testInitWithoutServletContext() {
        new Expectations() {{
            restPlugin.isEnabled();
            result = false;

            initContext.dependency(RestPlugin.class);
            result = restPlugin;
        }};

        underTest.init(initContext);
    }

    @Test
    public void testJerseyFilterPriority() {
        new Expectations() {{
            initContext.dependency(RestPlugin.class);
            result = restPlugin;

            restPlugin.isEnabled();
            result = true;

            restPlugin.getRestConfig();
            result = restConfig;

            initContext.dependencies(RestProvider.class);
            result = Lists.newArrayList(restProvider);
        }};

        underTest.init(initContext);
        assertThat(underTest.filters().get(0).getFilterClass()).isEqualTo(org.seedstack.seed.rest.jersey2.internal.SeedServletContainer.class);
        assertThat(underTest.filters().get(0).getPriority()).isEqualTo(SeedFilterPriority.JERSEY);
    }
}
