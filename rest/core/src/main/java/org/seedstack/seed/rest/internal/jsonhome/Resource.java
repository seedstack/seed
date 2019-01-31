/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.internal.jsonhome;

import static com.google.common.base.Preconditions.checkNotNull;

import com.damnhandy.uri.template.UriTemplate;
import com.damnhandy.uri.template.UriTemplateBuilder;
import java.util.HashMap;
import java.util.Map;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.rest.internal.RestErrorCode;

/**
 * The JSON-HOME representation of a REST resource as defined by the
 * <a href="http://tools.ietf.org/html/draft-nottingham-json-home-03#section-3">IETF draft</a>.
 *
 * @see org.seedstack.seed.rest.internal.jsonhome.JsonHome
 */
public class Resource {
    private final String rel;
    private final String href;
    private final String hrefTemplate;
    private final Map<String, String> pathParams = new HashMap<>();
    private final Map<String, String> queryParams = new HashMap<>();
    private final Hints hints;

    /**
     * Resource constructor.
     *
     * @param rel   the relation type
     * @param href  the href (can be an href-template)
     * @param hints the the resource hints
     */
    public Resource(String rel, String href, Hints hints) {
        this.rel = checkNotNull(rel, "The rel must not be null");
        this.href = checkNotNull(href, "The href must not be null");
        this.hrefTemplate = null;
        this.hints = hints;
    }

    /**
     * Resource constructor.
     *
     * @param rel          the the relation type
     * @param hrefTemplate the href template
     * @param pathParams   the href variables
     * @param hints        the the resource hints
     */
    public Resource(String rel, String hrefTemplate, Map<String, String> pathParams, Map<String, String> queryParams,
            Hints hints) {
        this.rel = checkNotNull(rel, "The rel must not be null");
        this.hrefTemplate = checkNotNull(hrefTemplate, "The hrefTemplate must not be null");
        this.href = null;
        if (pathParams != null) {
            this.pathParams.putAll(pathParams);
        }
        if (queryParams != null) {
            this.queryParams.putAll(queryParams);
        }
        this.hints = hints;
    }

    /**
     * Returns the relation type.
     *
     * @return rel
     */
    public String rel() {
        return rel;
    }

    /**
     * Returns the href. It can be empty if the path is templated.
     *
     * @return href
     */
    public String href() {
        return href;
    }

    /**
     * Return the href template. It's empty unless the path is templated.
     *
     * @return hrefTemplate
     */
    public String hrefTemplate() {
        UriTemplateBuilder uriTemplateBuilder = UriTemplate.buildFromTemplate(hrefTemplate);
        if (!queryParams.isEmpty()) {
            uriTemplateBuilder = uriTemplateBuilder.query(
                    queryParams.keySet().toArray(new String[queryParams.keySet().size()]));
        }
        return uriTemplateBuilder.build().getTemplate();
    }

    /**
     * Return the hrefVars. It's empty unless the path is templated.
     *
     * @return hrefVars
     */
    public Map<String, String> hrefVars() {
        Map<String, String> map = new HashMap<>(pathParams);
        map.putAll(queryParams);
        return map;
    }

    /**
     * Returns hints about the resource.
     *
     * @return the hints
     */
    public Hints hints() {
        return hints;
    }

    /**
     * Indicates whether the href is templated.
     *
     * @return true if the href is templated, false otherwise
     */
    public boolean templated() {
        return hrefTemplate != null && !"".equals(hrefTemplate);
    }

    /**
     * Merges the current resource with another resource instance.
     * The merge objects must represent the same resource but can
     * come from multiple methods.
     *
     * @param resource the resource object to merge
     */
    public void merge(Resource resource) {
        if (resource == null) {
            return;
        }

        checkRel(resource);
        checkHrefs(resource);

        if (resource.templated()) {
            this.pathParams.putAll(resource.pathParams);
            this.queryParams.putAll(resource.queryParams);
        }
        if (resource.hints() != null) {
            this.hints.merge(resource.hints());
        }
    }

    private void checkRel(Resource resource) {
        if (!rel.equals(resource.rel())) {
            throw SeedException.createNew(RestErrorCode.CANNOT_MERGE_RESOURCE_WITH_DIFFERENT_REL)
                    .put("oldRel", rel).put("newRel", resource.rel());
        }
    }

    private void checkHrefs(Resource resource) {
        if (resource == null) {
            throw new IllegalArgumentException("Resource should not be null");
        }

        if (this.templated() != resource.templated()) {
            throw SeedException.createNew(RestErrorCode.MULTIPLE_PATH_FOR_THE_SAME_REL)
                    .put("rel", rel)
                    .put("oldHref", this.hrefTemplate != null ? hrefTemplate : href)
                    .put("newHref", resource.hrefTemplate != null ? resource.hrefTemplate : resource.href);
        }

        if (this.templated()) {
            if (!resource.hrefTemplate.equals(this.hrefTemplate)) {
                throw SeedException.createNew(RestErrorCode.MULTIPLE_PATH_FOR_THE_SAME_REL)
                        .put("rel", rel)
                        .put("oldHref", this.hrefTemplate)
                        .put("newHref", resource.hrefTemplate);
            }

        } else {
            if (!resource.href().equals(this.href())) {
                throw SeedException.createNew(RestErrorCode.MULTIPLE_PATH_FOR_THE_SAME_REL)
                        .put("rel", rel)
                        .put("oldHref", this.href)
                        .put("newHref", resource.href);
            }
        }
    }

    /**
     * Serializes the resource into a map.
     *
     * @return the resource map
     */
    public Map<String, Object> toRepresentation() {
        Map<String, Object> representation = new HashMap<>();
        if (templated()) {
            representation.put("href-template", hrefTemplate);
            representation.put("href-vars", hrefVars());
        } else {
            representation.put("href", href);
        }
        if (hints != null) {
            Map<String, Object> hintsRepresentation = hints.toRepresentation();
            if (!hintsRepresentation.isEmpty()) {
                representation.put("hints", hintsRepresentation);
            }
        }
        return representation;
    }

    @Override
    public String toString() {
        return "Resource{" +
                "rel='" + rel + '\'' +
                ", href='" + href + '\'' +
                ", hrefTemplate='" + hrefTemplate + '\'' +
                ", hrefVars=" + hrefVars() +
                ", hints=" + hints +
                '}';
    }
}
