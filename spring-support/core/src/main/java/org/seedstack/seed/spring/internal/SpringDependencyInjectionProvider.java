/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.spring.internal;


import io.nuun.kernel.api.di.UnitModule;
import io.nuun.kernel.api.plugin.PluginException;
import io.nuun.kernel.core.internal.ModuleEmbedded;
import io.nuun.kernel.spi.DependencyInjectionProvider;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;

class SpringDependencyInjectionProvider implements DependencyInjectionProvider {

	@Override
    public boolean canHandle(Class<?> injectionDefinition) {
        return ConfigurableListableBeanFactory.class.isAssignableFrom(injectionDefinition) || ConfigurableApplicationContext.class.isAssignableFrom(injectionDefinition);
    }

    @Override
    public UnitModule convert(Object injectionDefinition) {
        if (injectionDefinition instanceof ConfigurableListableBeanFactory) {
            return ModuleEmbedded.wrap(new SpringModule((ConfigurableListableBeanFactory) injectionDefinition));
        } else if (injectionDefinition instanceof ConfigurableApplicationContext) {
            return ModuleEmbedded.wrap(new SpringModule(((ConfigurableApplicationContext) injectionDefinition).getBeanFactory()));
        } else {
            throw new PluginException("Only ConfigurableListableBeanFactory or ConfigurableApplicationContext types are handled");
        }
    }

    @Override
    public Object kernelDIProvider() {
        return null;
    }
}