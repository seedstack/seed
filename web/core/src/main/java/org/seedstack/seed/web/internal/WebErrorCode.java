/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web.internal;

import org.seedstack.shed.exception.ErrorCode;

/**
 * Enumerates all Web error codes.
 */
public enum WebErrorCode implements ErrorCode {
    CANNOT_RESOLVE_WEB_RESOURCE_LOCATION,
    ERROR_RETRIEVING_RESOURCE,
    UNABLE_TO_DETERMINE_RESOURCE_INFO,
    UNABLE_TO_SCAN_TOMCAT_JNDI_DIRECTORY,
    UNABLE_TO_SCAN_TOMCAT_JNDI_JAR,
    UNABLE_TO_SCAN_WEBSPHERE_DIRECTORY,
    CANNOT_PUBLISH_WEBSOCKET_ENDPOINT, UNEXPECTED_EXCEPTION
}
