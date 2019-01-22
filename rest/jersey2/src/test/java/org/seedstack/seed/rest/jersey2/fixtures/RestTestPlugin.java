/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.jersey2.fixtures;

import com.google.common.collect.Lists;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.InitContext;
import java.util.Collection;
import java.util.Locale;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Variant;
import org.seedstack.seed.core.internal.AbstractSeedPlugin;
import org.seedstack.seed.rest.internal.RestPlugin;

public class RestTestPlugin extends AbstractSeedPlugin {
    @Override
    public String name() {
        return "rest-test";
    }

    @Override
    public InitState initialize(InitContext initContext) {
        RestPlugin restPlugin = initContext.dependency(RestPlugin.class);

        if (getConfiguration().getOptional(Boolean.class, "textHome").orElse(true)) {
            restPlugin.addRootResourceVariant(new Variant(MediaType.TEXT_PLAIN_TYPE, (Locale) null, null),
                    TextRootResource.class);
        }

        return InitState.INITIALIZED;
    }

    @Override
    protected Collection<Class<?>> dependencies() {
        return Lists.newArrayList(RestPlugin.class);
    }
}
