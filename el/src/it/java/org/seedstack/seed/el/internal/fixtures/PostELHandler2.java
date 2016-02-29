/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.el.internal.fixtures;

import org.seedstack.seed.el.internal.ExpressionLanguageHandlerIT;
import org.seedstack.seed.el.spi.ELHandler;

/**
 * @author pierre.thirouin@ext.mpsa.com
 *         Date: 01/07/2014
 */
public class PostELHandler2 implements ELHandler<PostEL> {

    @Override
    public void handle(Object value) {
        if(String.class.isAssignableFrom(value.getClass())) {
            ExpressionLanguageHandlerIT.message = (String) value;
        } else {
            ExpressionLanguageHandlerIT.count += (Long) value;
        }
    }
}
