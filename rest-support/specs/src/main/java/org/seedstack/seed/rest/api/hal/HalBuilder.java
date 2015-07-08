/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.api.hal;

/**
 * Builder allowing to create an {@link HalRepresentation}.
 *
 * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
 */
public interface HalBuilder {

    /**
     * Creates a new HAL representation based on a given representation.
     *
     * @param representation the representation
     * @return the HAL representation
     */
    HalRepresentation create(Object representation);

}
