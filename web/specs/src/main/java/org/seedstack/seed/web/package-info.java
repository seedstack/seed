/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
/**
 * This package provides integration with the Java Servlet specification and offers various Web related features.
 * The execution container should at least provide a Servlet 2.5 compliance level but some features are only available
 * at Servlet 3.0 compliance level.
 *
 * SEED web support allows you to completely eliminate application web.xml configuration and take advantage of
 * annotation based servlet and filter declaration. It ties together dependency injection and the web components,
 * meaning that your servlets and filters can benefit from:
 *
 * <ul>
 *     <li>Injection,</li>
 *     <li>Type-safe configuration,</li>
 *     <li>Modularization of servlet and filters (even if your container doesn't support web fragments),</li>
 *     <li>Aspect Oriented Programming,</li>
 * </ul>
 *
 * …while you can still benefit of the standard servlet lifecycle.
 */
package org.seedstack.seed.web;