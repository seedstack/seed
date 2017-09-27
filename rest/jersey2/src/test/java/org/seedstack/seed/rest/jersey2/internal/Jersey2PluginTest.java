/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.rest.jersey2.internal;

import com.google.common.collect.Lists;
import io.nuun.kernel.api.plugin.context.InitContext;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.StrictExpectations;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.seedstack.seed.rest.RestConfig;
import org.seedstack.seed.rest.internal.RestPlugin;
import org.seedstack.seed.rest.spi.RestProvider;
import org.seedstack.seed.web.internal.WebPlugin;

@RunWith(JMockit.class)
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
        new StrictExpectations() {{
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
        new NonStrictExpectations() {{
            restPlugin.isEnabled();
            result = false;

            initContext.dependency(RestPlugin.class);
            result = restPlugin;

            initContext.dependencies(RestProvider.class);
            result = Lists.newArrayList(restProvider);

            restProvider.resources();
            result = null;
            restProvider.providers();
            result = null;
        }};

        underTest.init(initContext);
    }
}
