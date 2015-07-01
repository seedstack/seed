/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
/**
 *
 */
package org.seedstack.seed.persistence.jpa.internal;

import org.seedstack.seed.core.api.ErrorCode;

/**
 * @author redouane.loulou@ext.mpsa.com
 */
enum JpaErrorCode implements ErrorCode {
    DATA_SOURCE_NOT_FOUND,
    NO_PERSISTED_CLASSES_IN_UNIT,
    ACCESSING_ENTITY_MANAGER_OUTSIDE_TRANSACTION
}