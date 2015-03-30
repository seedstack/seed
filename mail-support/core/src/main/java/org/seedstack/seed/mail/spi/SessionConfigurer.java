/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.mail.spi;

import javax.mail.Session;
import java.util.Map;

/**
 * interface for handling session configuration.
 * <p>
 * must be implemented by classes which wishes to provide configured sessions to be used elsewhere
 * Created by E442250 on 05/05/2014.
 */
public interface SessionConfigurer {
    /**
     * this method configure the sessions based on properties provided
     * in the configuration files
     *
     * @return a map of sessions with provider protocol as key and the given session as value
     */
    Map<String, Session> doConfigure();
}
