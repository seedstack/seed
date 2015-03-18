/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.jms;

import com.google.inject.Module;
import org.seedstack.seed.it.SeedITRunner;
import io.nuun.kernel.api.Plugin;
import org.junit.runners.model.InitializationError;

/**
 * @author Pierre Thirouin <pierre.thirouin@ext.mpsa.com>
 *         07/11/2014
 */
public class CustomITRunner extends SeedITRunner {

    public CustomITRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }

    public CustomITRunner(Class<?> klass, Class<? extends Plugin>[] safeModePlugins, Module... safeModeModules) throws InitializationError {
        super(klass, safeModePlugins, safeModeModules);
    }
}
