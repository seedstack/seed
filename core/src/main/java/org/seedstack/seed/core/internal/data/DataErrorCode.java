/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
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
    EXPORT_FAILED,
    FAILED_TO_COMMIT_IMPORT,
    FAILED_TO_PARSE_DATA_STREAM,
    FAILED_TO_ROLLBACK_IMPORT,
    IMPORT_FAILED,
    MISSING_TYPE_PARAMETER,
    NO_EXPORTER_FOUND,
    NO_IMPORTER_FOUND,
    UNEXPECTED_DATA_TYPE
}
