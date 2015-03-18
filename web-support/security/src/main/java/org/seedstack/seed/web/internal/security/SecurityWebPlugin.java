/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web.internal.security;

import com.google.inject.Module;
import io.nuun.kernel.api.plugin.PluginException;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.api.plugin.request.ClasspathScanRequestBuilder;
import jodd.props.Props;
import org.apache.shiro.guice.web.ShiroWebModule;
import org.seedstack.seed.core.internal.application.ApplicationPlugin;
import org.seedstack.seed.security.internal.SeedSecurityPlugin;
import org.seedstack.seed.web.api.security.SecurityFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.ServletContext;
import java.util.ArrayList;
import java.util.Collection;

/**
 * This plugins adds web security.
 *
 * @author yves.dautremay@mpsa.com
 */
public class SecurityWebPlugin implements SeedSecurityPlugin {
	private static final Logger LOGGER = LoggerFactory.getLogger(SecurityWebPlugin.class);

	private ServletContext servletContext;

	private Collection<Class<? extends Filter>> scannedFilters = new ArrayList<Class<? extends Filter>>();

	private String applicationId;

    private Props props;

	@SuppressWarnings("unchecked")
	@Override
	public void init(InitContext initContext) {
		ApplicationPlugin applicationPlugin = (ApplicationPlugin) initContext.pluginsRequired().iterator().next();
		props = applicationPlugin.getProps();
		applicationId = applicationPlugin.getApplication().getId();
		for (Class<?> filterClass : initContext.scannedClassesByAnnotationClass().get(SecurityFilter.class)) {
			if (Filter.class.isAssignableFrom(filterClass)) {
				scannedFilters.add((Class<? extends Filter>) filterClass);
			} else {
				throw new PluginException("Annotated class " + filterClass.getName() + " must implement Filter to be used in a filter chain");
			}
		}
	}

	@Override
	public void provideContainerContext(Object containerContext) {
		if (containerContext instanceof ServletContext) {
			servletContext = (ServletContext) containerContext;
		}
	}

	@Override
	public void classpathScanRequests(ClasspathScanRequestBuilder classpathScanRequestBuilder) {
		classpathScanRequestBuilder.annotationType(SecurityFilter.class);
	}

	@Override
	public Module provideShiroModule() {
		if (servletContext != null) {
			return new SecurityWebModule(servletContext, props, scannedFilters, applicationId);
		} else {
            LOGGER.warn("No servlet context could be found; web security disabled.");
        }
		return null;
	}

	@Override
	public Collection<Module> provideOtherModules() {
		Collection<Module> modules = new ArrayList<Module>();
		if (servletContext != null) {
            modules.add(ShiroWebModule.guiceFilterModule());
        }
		return modules;
	}

}