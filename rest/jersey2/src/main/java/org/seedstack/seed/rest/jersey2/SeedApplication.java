/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.jersey2;

import org.glassfish.jersey.server.ResourceConfig;

/**
 * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
 */
public class SeedApplication extends ResourceConfig {

    public SeedApplication() {
       packages("org.seedstack.seed");
    }
}
