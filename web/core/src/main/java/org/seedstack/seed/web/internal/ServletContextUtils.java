/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.web.internal;

import com.google.inject.Injector;
import io.nuun.kernel.api.Kernel;
import javax.servlet.ServletContext;

public final class ServletContextUtils {
    public static final String KERNEL_ATTRIBUTE_NAME = Kernel.class.getName();
    public static final String INJECTOR_ATTRIBUTE_NAME = Injector.class.getName();

    private ServletContextUtils() {
        // no instantiation
    }

    public static Injector getInjector(ServletContext servletContext) {
        return (Injector) servletContext.getAttribute(INJECTOR_ATTRIBUTE_NAME);
    }

    public static Kernel getKernel(ServletContext servletContext) {
        return (Kernel) servletContext.getAttribute(KERNEL_ATTRIBUTE_NAME);
    }
}
