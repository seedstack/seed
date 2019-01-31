/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
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
import java.util.Optional;

/**
 * Defines a Link representation as described by the
 * <a href=https://tools.ietf.org/html/draft-kelly-json-hal-06#section-5>HAL specification</a>.
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
    // this map must only contain strings to allow clean copy
    private Map<String, Object> hrefVars = new HashMap<>();

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
        this.hrefVars = new HashMap<>(link.hrefVars);
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
     * @return itself
     */
    public Link type(String type) {
        this.type = type;
        return this;
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
        if (isTemplated()) {
            return href;
        } else {
            return Optional.ofNullable(href)
                    .map(UriTemplate::fromTemplate)
                    .map(uriTemplate -> uriTemplate.expand(hrefVars))
                    .orElse(null);
        }
    }

    public Link set(String variableName, Object value) {
        hrefVars.put(variableName, value.toString());
        return this;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Link link = (Link) o;

        if (templated != link.templated) return false;
        if (href != null ? !href.equals(link.href) : link.href != null) return false;
        if (type != null ? !type.equals(link.type) : link.type != null) return false;
        if (deprecation != null ? !deprecation.equals(link.deprecation) : link.deprecation != null) return false;
        if (name != null ? !name.equals(link.name) : link.name != null) return false;
        if (profile != null ? !profile.equals(link.profile) : link.profile != null) return false;
        if (title != null ? !title.equals(link.title) : link.title != null) return false;
        if (hrefLang != null ? !hrefLang.equals(link.hrefLang) : link.hrefLang != null) return false;
        return hrefVars.equals(link.hrefVars);

    }

    @Override
    public int hashCode() {
        int result = href != null ? href.hashCode() : 0;
        result = 31 * result + (templated ? 1 : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (deprecation != null ? deprecation.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (profile != null ? profile.hashCode() : 0);
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (hrefLang != null ? hrefLang.hashCode() : 0);
        result = 31 * result + hrefVars.hashCode();
        return result;
    }
}
