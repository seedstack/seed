/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.internal.hal;

import java.util.HashMap;
import java.util.Map;
import org.seedstack.seed.rest.RelRegistry;
import org.seedstack.seed.rest.hal.Link;

public class RelRegistryImpl implements RelRegistry {

    private final Map<String, Link> linksByRel;

    public RelRegistryImpl(Map<String, Link> linksByRel) {
        if (linksByRel != null) {
            this.linksByRel = linksByRel;
        } else {
            this.linksByRel = new HashMap<>();
        }
    }

    @Override
    public String href(String rel) {
        return uri(rel).getHref();
    }

    @Override
    public Link uri(String rel) {
        if (rel == null || rel.equals("")) {
            throw new IllegalArgumentException("rel can't be blank");
        }
        Link link = linksByRel.get(rel);
        if (link == null) {
            throw new IllegalArgumentException("Unknown rel " + rel);
        }
        return new Link(link);
    }
}
