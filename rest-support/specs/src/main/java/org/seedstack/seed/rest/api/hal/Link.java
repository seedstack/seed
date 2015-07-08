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

import java.net.URI;

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
    private String deprecation;
    private String name;
    private URI profile;
    private String title;
    private String hrefLang;

    /**
     * Default constructor required by Jackson.
     */
    Link() {
    }

    public Link(String href) {
        this.href = href;
    }

    public Link templated() {
        this.templated = true;
        return this;
    }

    public Link deprecate(String deprecation) {
        this.deprecation = deprecation;
        return this;
    }

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

    public boolean isTemplated() {
        return templated;
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
