/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.data;

import org.seedstack.seed.ErrorCode;

/**
 * SEED core data error codes.
 *
 * @author adrien.lauer@mpsa.com
 * @author pierre.thirouin@ext.mpsa.com
 */
enum DataErrorCode implements ErrorCode {
    FAILED_TO_PARSE_DATA_STREAM,
    NO_EXPORTER_FOUND,
    IMPORT_FAILED,
    NO_IMPORTER_FOUND,
    UNEXPECTED_DATA_TYPE,
    FAILED_TO_ROLLBACK_IMPORT,
    FAILED_TO_COMMIT_IMPORT,
    EXPORT_FAILED,
    MISSING_TYPE_PARAMETER
}
