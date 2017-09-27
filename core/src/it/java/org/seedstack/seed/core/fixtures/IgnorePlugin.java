/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.fixtures;

import io.nuun.kernel.api.plugin.request.BindingRequest;
import io.nuun.kernel.core.AbstractPlugin;
import java.util.Collection;
import org.seedstack.seed.core.IgnoreIT;

public class IgnorePlugin extends AbstractPlugin {

    @Override
    public String name() {
        return "ignore-plugin";
    }

    @Override
    public String pluginPackageRoot() {
        return "org.seedstack";
    }

    @Override
    public Collection<BindingRequest> bindingRequests() {
        return bindingRequestsBuilder().annotationType(IgnoreIT.Scan.class).build();
    }
}
