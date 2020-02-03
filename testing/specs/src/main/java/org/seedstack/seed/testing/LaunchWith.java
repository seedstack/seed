/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.testing;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.seedstack.seed.spi.SeedLauncher;

/**
 * This annotation can be used to specify the {@link SeedLauncher} used to launch the tested application. It can also
 * alter the launch mode.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface LaunchWith {
    /**
     * The {@link SeedLauncher} implementation used for launching the tested application.
     *
     * @return the launcher class.
     */
    Class<? extends SeedLauncher> value() default SeedLauncher.class;

    /**
     * The launch mode of the tested application.
     *
     * @return the launch mode.
     */
    LaunchMode mode() default LaunchMode.ANY;

    /**
     * If the launch should occur in a separate thread.
     *
     * @return if true, the launch will be done in a new thread, otherwise in the main thread.
     */
    boolean separateThread() default false;
}
