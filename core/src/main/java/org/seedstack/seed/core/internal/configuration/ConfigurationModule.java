/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.internal.configuration;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;
import org.seedstack.seed.Application;

class ConfigurationModule extends AbstractModule {
    private final Application application;

    ConfigurationModule(Application application) {
        this.application = application;
    }

    @Override
    protected void configure() {
        bind(Application.class).toInstance(this.application);
        bindListener(Matchers.any(), new ConfigurationTypeListener(application.getConfiguration()));
    }
}
