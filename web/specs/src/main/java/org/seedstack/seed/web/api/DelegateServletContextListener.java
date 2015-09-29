/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web.api;

import javax.servlet.ServletContextAttributeListener;
import javax.servlet.ServletContextListener;

/**
 * This interfaces marks servlet context listeners that will be:
 * <ul>
 *     <li>Automatically detected by SEED,</li>
 *     <li>Initialized after SEED context listener is initialized,</li>
 *     <li>Destroyed after SEED context listener is destroyed.</li>
 * </ul>
 *
 * These contexts have their members injectable (fields and methods), but not their constructors).
 *
 * @author yves.dautremay@mpsa.com
 * @author epo.jemba@ext.mpsa.com
 * @author adrien.lauer@mpsa.com
 */
public interface DelegateServletContextListener extends ServletContextListener, ServletContextAttributeListener {

}
