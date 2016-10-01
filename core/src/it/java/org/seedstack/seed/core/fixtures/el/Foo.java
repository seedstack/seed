/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.fixtures.el;

/**
 * @author pierre.thirouin@ext.mpsa.com
 *         Date: 27/06/2014
 */
public class Foo {
    private String message = "hello";

    @AnnotationWithoutValue
    public String pok() {
        return message;
    }

}
