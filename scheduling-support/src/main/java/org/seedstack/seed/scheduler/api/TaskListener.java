/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.scheduler.api;

/**
 * This class provides methods to listen an executed {@link Task}.
 *
 * @param <T> the {@code Task} to listen to (<b>MANDATORY</b>)
 * @author pierre.thirouin@ext.mpsa.com
 *         Date: 10/01/14
 */
public interface TaskListener<T extends Task> {

    /**
     * Called before the {@code Task} is executed.
     *
     * @param sc the associated scheduling context
     */
    void before(SchedulingContext sc);

    /**
     * Called after the {@code Task} is executed.
     *
     * @param sc the associated scheduling context
     */
    void after(SchedulingContext sc);

    /**
     * Called if any exception occurs.
     *
     * @param sc the associated scheduling context
     * @param e the exception thrown by the task
     */
    void onException(SchedulingContext sc, Exception e);
}
