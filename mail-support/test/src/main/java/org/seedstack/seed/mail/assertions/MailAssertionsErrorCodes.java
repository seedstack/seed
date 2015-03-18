/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.mail.assertions;

import org.seedstack.seed.core.api.ErrorCode;

enum MailAssertionsErrorCodes implements ErrorCode {
    NO_MAIL_PROVIDER_FOUND,
    NO_SUCH_PROVIDER_FOUND,
    NO_MAIL_SUBJECT_SPECIFIED,
    NO_RECIPIENTS_SPECIFIED,
    NOT_SAME_RECIPIENTS_SIZE,
    ERROR_OCCURED_WHILE_EXTRACTING_MESSAGE_FROM_SERVER,
    NO_SENT_DATE_FOUND
}
