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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
 */
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class HalRepresentation {

    @JsonProperty("_links")
    protected final Map<String, Link> links = new HashMap<String, Link>();

    @JsonProperty("_embedded")
    protected final Map<String, Object> embedded = new HashMap<String, Object>();

    /**
     * Default constructor required by Jackson.
     */
    protected HalRepresentation() {
    }

    public Map<String, Link> getLinks() {
        return links;
    }

    public HalRepresentation self(String href) {
        this.links.put("self", new Link(href));
        return this;
    }

    public HalRepresentation link(String rel, String href) {
        this.links.put(rel, new Link(href));
        return this;
    }

    public HalRepresentation link(String rel, Link link) {
        this.links.put(rel, link);
        return this;
    }

    public Map<String, Object> getEmbedded() {
        return embedded;
    }

    public HalRepresentation embedded(String rel, Object embedded) {
        this.embedded.put(rel, embedded);
        return this;
    }
}
