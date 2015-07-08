/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.api;

import org.seedstack.seed.rest.api.hal.Link;

/**
 * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
 */
public interface RelRegistry {

    /**
     * Finds the link representation associated to a rel. Rel are found by scanning the class for the
     * {@link org.seedstack.seed.rest.api.Rel} annotation
     *
     * @param rel the relation type
     * @return a link or null if the rel is not found
     */
    Link linkForRel(String rel);
}
