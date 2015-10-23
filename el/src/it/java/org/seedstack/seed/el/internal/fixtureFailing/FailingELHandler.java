/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.el.internal.fixtureFailing;

import org.seedstack.seed.el.spi.ELHandler;
import org.seedstack.seed.el.internal.ExpressionLanguageHandlerIT;
import org.seedstack.seed.el.internal.ExpressionLanguageHandlerIT;

/**
 * @author pierre.thirouin@ext.mpsa.com
 *         Date: 01/07/2014
 */
public class FailingELHandler implements ELHandler<AnnotationWithoutValue> {

    @Override
    public void handle(Object value) {
        if(String.class.isAssignableFrom(value.getClass())) {
            ExpressionLanguageHandlerIT.message = (String) value;
        } else {
            ExpressionLanguageHandlerIT.count = (Long) value;
        }
    }
}
