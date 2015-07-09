/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.internal.hal;

import org.seedstack.seed.rest.api.RelRegistry;
import org.seedstack.seed.rest.api.hal.Link;

import java.util.HashMap;
import java.util.Map;

/**
 * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
 */
public class RelRegistryImpl implements RelRegistry {

    private final Map<String, Link> linksByRel;

    public RelRegistryImpl(Map<String, Link> linksByRel) {
        if (linksByRel != null) {
            this.linksByRel = linksByRel;
        } else {
            this.linksByRel = new HashMap<String, Link>();
        }
    }

    @Override
    public String href(String rel) {
        if (rel == null || rel.equals("")) {
            throw new IllegalArgumentException("rel can't be blank");
        }
        Link link = linksByRel.get(rel);
        if (link != null) {
            return link.getHref();
        } else {
            return null;
        }
    }

    @Override
    public Link link(String rel) {
        if (rel == null || rel.equals("")) {
            throw new IllegalArgumentException("rel can't be blank");
        }
        return linksByRel.get(rel);
    }
}
