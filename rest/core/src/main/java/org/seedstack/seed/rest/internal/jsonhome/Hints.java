/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.rest.internal.jsonhome;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.rest.internal.RestErrorCode;

/**
 * Defines the hints representation as defined by the
 * <a href="http://tools.ietf.org/html/draft-nottingham-json-home-03#section-4">IETF draft</a>.
 *
 * @see org.seedstack.seed.rest.internal.jsonhome.JsonHome
 */
public class Hints {

    private List<String> allow = new ArrayList<>();

    private Map<String, Object> formats = new TreeMap<>();

    private List<String> acceptPath = new ArrayList<>();

    private List<String> acceptPost = new ArrayList<>();

    private List<String> acceptRanges = new ArrayList<>();

    private List<String> acceptPrefer = new ArrayList<>();

    private URI docs;

    private List<String> preconditionReq = new ArrayList<>();

    private List<AuthorizationRequired> authReq = new ArrayList<>();

    private Status status;

    /**
     * Merges the current hints with hints comming from another method of the resource.
     *
     * @param hints the hints to merge
     */
    public void merge(Hints hints) {
        this.allow.addAll(hints.getAllow());
        this.formats.putAll(hints.getFormats());
        this.acceptPath.addAll(hints.getAcceptPath());
        this.acceptPost.addAll(hints.getAcceptPost());
        this.acceptRanges.addAll(hints.getAcceptRanges());
        this.acceptPrefer.addAll(hints.getAcceptPrefer());
        mergeDocs(hints);
        preconditionReq.addAll(hints.getPreconditionReq());
        authReq.addAll(hints.getAuthReq());
        mergeStatus(hints);
    }

    /**
     * Merges docs. If the other resource has a different docs link, it throws a {@code SeedException}.
     *
     * @param hints the hints to merge
     */
    private void mergeDocs(Hints hints) {
        if (docs != null) {
            if (hints.getDocs() != null && !docs.equals(hints.getDocs())) {
                throw SeedException.createNew(RestErrorCode.CANNOT_MERGE_RESOURCES_WITH_DIFFERENT_DOC)
                        .put("oldDoc", docs).put("newDoc", hints.getDocs());
            }
        } else {
            this.docs = hints.getDocs();
        }
    }

    /**
     * Merges the status. If one of the resource have different status the status will be
     * chosen according to the following priorities:
     * <ol>
     * <li>GONE</li>
     * <li>DEPRECATED</li>
     * <li>NONE</li>
     * </ol>
     *
     * @param hints the hints to merge
     */
    private void mergeStatus(Hints hints) {
        if (status != null) {
            if (hints.getStatus() != null && !status.equals(hints.getStatus())) {
                switch (hints.getStatus()) {
                    case GONE:
                        status = Status.GONE;
                        break;
                    case DEPRECATED:
                        if (status != Status.GONE) {
                            status = Status.DEPRECATED;
                        }
                        break;
                }
            }
        } else {
            this.status = hints.getStatus();
        }
    }

    public List<String> getAllow() {
        return allow;
    }

    public void addAllow(String allow) {
        this.allow.add(allow);
    }

    public Map<String, Object> getFormats() {
        return formats;
    }

    public void format(String mediaType, Object representation) {
        this.formats.put(mediaType, representation);
    }

    public List<String> getAcceptPath() {
        return acceptPath;
    }

    public void acceptPath(String acceptPath) {
        this.acceptPath.add(acceptPath);
    }

    public List<String> getAcceptPost() {
        return acceptPost;
    }

    public void acceptPost(String acceptPost) {
        this.acceptPost.add(acceptPost);
    }

    public List<String> getAcceptRanges() {
        return acceptRanges;
    }

    public void acceptRanges(String acceptRanges) {
        this.acceptRanges.add(acceptRanges);
    }

    public List<String> getAcceptPrefer() {
        return acceptPrefer;
    }

    public void acceptPrefer(String acceptPrefer) {
        this.acceptPrefer.add(acceptPrefer);
    }

    public URI getDocs() {
        return docs;
    }

    public void setDocs(URI docs) {
        this.docs = docs;
    }

    public List<String> getPreconditionReq() {
        return preconditionReq;
    }

    public void preconditionReq(String preconditionReq) {
        this.preconditionReq.add(preconditionReq);
    }

    public List<AuthorizationRequired> getAuthReq() {
        return authReq;
    }

    public void authReq(AuthorizationRequired authReq) {
        this.authReq.add(authReq);
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Map<String, Object> toRepresentation() {
        HashMap<String, Object> repr = new HashMap<>();
        addOptional(repr, "allow", allow);
        addOptional(repr, "formats", formats);
        addOptional(repr, "accept-patch", acceptPath);
        addOptional(repr, "accept-post", acceptPost);
        addOptional(repr, "accept-ranges", acceptRanges);
        addOptional(repr, "accept-prefer", acceptPrefer);
        addOptional(repr, "docs", docs);
        addOptional(repr, "precondition-req", preconditionReq);
        addOptional(repr, "auth-req", authReq);
        addOptional(repr, "status", status);
        return repr;
    }

    private void addOptional(Map<String, Object> representation, String name, Object object) {
        if (object == null) {
            return;
        }
        boolean isEmptyCollection = Collection.class.isAssignableFrom(
                object.getClass()) && ((Collection) object).isEmpty();
        boolean isEmptyMap = Map.class.isAssignableFrom(object.getClass()) && ((Map) object).isEmpty();

        if (!isEmptyCollection && !isEmptyMap) {
            representation.put(name, object);
        }
    }

    public enum Status {
        GONE,
        DEPRECATED
    }
}
