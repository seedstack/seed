/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.persistence.solr.internal;

final class SolrConfigurationConstants {
    private SolrConfigurationConstants() {
    }

    // all
    public static final String URL = "url";
    public static final String URLS = "urls";
    public static final String TYPE = "type";

    // http and lb http
    public static final String CONNECTION_TIMEOUT = "connection-timeout";
    public static final String QUERY_PARAMS = "query-params";
    public static final String SO_TIMEOUT = "so-timeout";

    // lb http
    public static final String ALIVE_CHECK_INTERVAL = "alive-check-interval";

    // http
    public static final String ALLOW_COMPRESSION = "allow-compression";
    public static final String MAX_CONNECTIONS_PER_HOST = "max-connections-per-host";
    public static final String FOLLOW_REDIRECTS = "follow-redirects";
    public static final String MAX_TOTAL_CONNECTIONS = "max-total-connections";
    public static final String USE_MULTI_PART_HOST = "use-multi-part-host";

    // cloud
    public static final String LB_URLS = "lb-urls";
    public static final String ZK_CLIENT_TIMEOUT = "zk-client-timeout";
    public static final String ZK_CONNECT_TIMEOUT = "zk-connect-timeout";
    public static final String PARALLEL_UPDATES = "parallel-updates";
    public static final String PARALLEL_CACHE_REFRESHES = "parallel-cache-refreshes";
    public static final String COLLECTION_CACHE_TTL = "collection-cache-ttl";
    public static final String DEFAULT_COLLECTION = "default-collection";
    public static final String ID_FIELD = "id-field";
    public static final String UPDATE_TO_LEADERS = "update-to-leaders";
    public static final String CHROOT = "chroot";
}
