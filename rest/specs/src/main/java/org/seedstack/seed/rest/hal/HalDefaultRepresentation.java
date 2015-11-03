/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.hal;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

/**
 * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
 */
public class HalDefaultRepresentation extends HalRepresentation {

    @JsonUnwrapped
    private Object resource;

    HalDefaultRepresentation() {
    }

    public HalDefaultRepresentation(Object resource) {
        this.resource = resource;
    }

    public Object getResource() {
        return resource;
    }
}
