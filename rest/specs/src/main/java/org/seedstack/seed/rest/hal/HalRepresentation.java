/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.hal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An HAL resource representation as defined in the
 * <a href="https://tools.ietf.org/html/draft-kelly-json-hal-06#section-3">RFC specification</a>.
 * <p>
 * It's a resource object with two reserved properties:
 * </p>
 * <ul>
 * <li>"_links": links to other resources</li>
 * <li>"_embedded": embedded resources</li>
 * </ul>
 *
 * For instance:
 * <pre>
 * GET /orders/523 HTTP/1.1
 * Host: example.org
 * Accept: application/hal+json
 *
 * HTTP/1.1 200 OK
 * Content-Type: application/hal+json
 *
 * {
 *   "_links": {
 *     "self": { "href": "/orders/523" },
 *     "warehouse": { "href": "/warehouse/56" },
 *     "invoice": { "href": "/invoices/873" }
 *   },
 *   "currency": "USD",
 *   "status": "shipped",
 *   "total": 10.20
 * }
 * </pre>
 */
public class HalRepresentation {

    /**
     * Links as defined in the
     * <a href="https://tools.ietf.org/html/draft-kelly-json-hal-06#section-4.1.1">specification</a>
     * is a map whose keys are rel and values can be either an object link or an array of object links.
     * <p>
     * So Object can be a List&lt;Link&gt; or a {@link Link}
     * </p>
     */
    @JsonProperty("_links")
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private final Map<String, Object> links = new HashMap<>();

    @JsonProperty("_embedded")
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private final Map<String, Object> embedded = new HashMap<>();

    /**
     * Default constructor required by Jackson.
     */
    public HalRepresentation() {
    }

    /**
     * Adds the self link.
     *
     * @param href the resource href
     * @return itself
     */
    public HalRepresentation self(String href) {
        addLink("self", new Link(href));
        return this;
    }

    /**
     * Adds the self link.
     *
     * @param link the link
     * @return itself
     */
    public HalRepresentation self(Link link) {
        addLink("self", link);
        return this;
    }

    /**
     * Adds a link with the specified rel and href.
     *
     * @param rel  the rel
     * @param href the href
     * @return itself
     */
    public HalRepresentation link(String rel, String href) {
        addLink(rel, new Link(href));
        return this;
    }

    /**
     * Adds a link with the specified rel and link representation.
     *
     * @param rel  the rel
     * @param link the link representation
     * @return itself
     */
    public HalRepresentation link(String rel, Link link) {
        addLink(rel, link);
        return this;
    }

    /**
     * Returns the resource's links.
     *
     * @return map of links
     */
    public Object getLink(String rel) {
        return links.get(rel);
    }

    /**
     * Returns the resource's links.
     *
     * @return map of links
     */
    public Map<String, Object> getLinks() {
        return links;
    }

    @SuppressWarnings("unchecked")
    private void addLink(String rel, Link link) {
        Object obj = links.get(rel);
        if (obj == null) {
            links.put(rel, link);
        } else if (obj instanceof List) {
            List<Link> linksForRel = (List<Link>) obj;
            linksForRel.add(link);
            links.put(rel, linksForRel);
        } else if (obj instanceof Link) {
            Link linkCopy = (Link) obj;
            List<Link> linksForRel = new ArrayList<>();
            linksForRel.add(linkCopy);
            linksForRel.add(link);
            links.put(rel, linksForRel);
        }
    }

    /**
     * Adds an embedded resource or array of resources.
     *
     * @param rel      the relation type
     * @param embedded the resource (can be an array of resources)
     * @return itself
     */
    public HalRepresentation embedded(String rel, Object embedded) {
        this.embedded.put(rel, embedded);
        return this;
    }

    /**
     * Returns the embedded resources.
     *
     * @return the map of embedded resources
     */
    public Map<String, Object> getEmbedded() {
        return embedded;
    }
}
