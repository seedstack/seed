/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.mail.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Any class annotated with annotation would benefit of a mock mail
 * server instance launched which can be used to test against sending mails
 *
 * @author aymen.benhmida@ext.mpsa.com
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface WithMailServer {

    /**
     * port configuration for the mail server
     *
     * @return
     */
    int port() default 25;

    /**
     * the host name to be used by client to connect to the mail server
     *
     * @return
     */
    String host();
}
