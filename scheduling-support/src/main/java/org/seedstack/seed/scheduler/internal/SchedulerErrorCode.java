/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.scheduler.internal;

import org.seedstack.seed.core.api.ErrorCode;

/**
 * Error code for the scheduler support.
 *
 * @author pierre.thirouin@ext.mpsa.com
 *         Date: 09/01/14
 */
enum SchedulerErrorCode implements ErrorCode {

    SCHEDULER_ERROR,
    UNRECOGNIZED_TRIGGER,
    MISSING_CRON_EXPRESSION,
    IMPOSSIBLE_TO_USE_CRON_AND_TRIGGER,
    SCHEDULER_FAILED_TO_SHUTDOWN,
    SCHEDULER_FAILED_TO_START,
    FAILED_TO_INSTANTIATE_TASK,
    JOB_EXECUTION_EXCEPTION,
    MISSING_TYPE_PARAMETER,
    EXCEPTION_IN_LISTENER, TRIGGER_AND_JOB_NAME_SHOULD_BE_UNIQUE
}
