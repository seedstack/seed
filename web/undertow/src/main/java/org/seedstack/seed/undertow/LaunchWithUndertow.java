/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.undertow;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.seedstack.seed.testing.ConfigurationProperty;
import org.seedstack.seed.testing.LaunchWith;
import org.seedstack.seed.undertow.internal.UndertowLauncher;

/**
 * This annotation can be used to launch the tested application with the Undertow embedded server. The server will be
 * run on an available port in the range 49152-65535 if not overridden in configuration.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@LaunchWith(value = UndertowLauncher.class, separateThread = true)
@ConfigurationProperty(name = "web.server.port", value = "$availableTcpPort()")
public @interface LaunchWithUndertow {
}
