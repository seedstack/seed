/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.web.internal;

import org.seedstack.seed.ErrorCode;

/**
 * Enumerates all Web error codes.
 *
 * @author adrien.lauer@mpsa.com
 */
public enum WebErrorCode implements ErrorCode {
    UNABLE_TO_SCAN_TOMCAT_JNDI_JAR,
    UNABLE_TO_SCAN_TOMCAT_JNDI_DIRECTORY,
    UNEXPECTED_EXCEPTION,
    ERROR_RETRIEVING_RESOURCE,
    PLUGIN_NOT_FOUND,
    UNABLE_TO_DETERMINE_RESOURCE_INFO,
    UNABLE_TO_SCAN_WEBSPHERE_DIRECTORY
}
