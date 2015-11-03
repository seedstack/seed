/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.hal;

import com.damnhandy.uri.template.UriTemplate;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Defines a Link representation as described by the
 * <a href=https://tools.ietf.org/html/draft-kelly-json-hal-06#section-5>HAL specification</a>.
 *
 * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
 */
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class Link {

    private String href;
    private boolean templated = false;
    private String type;
    private String deprecation;
    private String name;
    private URI profile;
    private String title;
    private String hrefLang;

    private Map<String, Object> hrefVars = new HashMap<String, Object>();

    /**
     * Default constructor required by Jackson.
     */
    Link() {
    }

    /**
     * Constructor.
     *
     * @param href the href
     */
    public Link(String href) {
        this.href = href;
    }

    /**
     * Copy constructor.
     *
     * @param link link to copy
     */
    public Link(Link link) {
        this.href = link.href;
        this.templated = link.templated;
        this.type = link.type;
        this.deprecation = link.deprecation;
        this.name = link.name;
        this.profile = link.profile;
        this.title = link.title;
        this.hrefLang = link.hrefLang;
        this.hrefVars = link.hrefVars;
    }

    /**
     * Indicates that the href is templated.
     *
     * @return itself
     */
    public Link templated() {
        this.templated = true;
        return this;
    }

    /**
     * Indicates that the resource is deprecated and
     * specify the URI for deprecation information.
     *
     * @param deprecation the deprecation URI
     * @return itself
     */
    public Link deprecate(String deprecation) {
        this.deprecation = deprecation;
        return this;
    }

    /**
     * Indicates the media type used by the resource.
     *
     * @param type the media type
     */
    public void type(String type) {
        this.type = type;
    }

    /**
     * Specifies an additional name for the link.
     *
     * @param name the link name
     * @return itself
     */
    public Link name(String name) {
        this.name = name;
        return this;
    }

    public Link profile(URI profile) {
        this.profile = profile;
        return this;
    }

    public Link title(String title) {
        this.title = title;
        return this;
    }

    public Link hrefLang(String hrefLang) {
        this.hrefLang = hrefLang;
        return this;
    }

    public String getHref() {
        return href;
    }

    public Link set(String variableName, Object value) {
        Link link = new Link(this);
        link.hrefVars.put(variableName, value);
        return link;
    }

    public String expand() {
        return UriTemplate.fromTemplate(href).expand(hrefVars);
    }

    public boolean isTemplated() {
        return templated;
    }

    public String getType() {
        return type;
    }

    public String getDeprecation() {
        return deprecation;
    }

    public String getName() {
        return name;
    }

    public URI getProfile() {
        return profile;
    }

    public String getTitle() {
        return title;
    }

    public String getHrefLang() {
        return hrefLang;
    }
}
