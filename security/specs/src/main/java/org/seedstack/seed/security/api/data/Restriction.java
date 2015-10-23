/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.api.data;

import org.seedstack.seed.security.spi.data.DataObfuscationHandler;
import org.seedstack.seed.security.spi.data.NullifyObfuscationHandler;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation will trigger the specified obfuscation when the expression evaluates to false.
 *
 * @author epo.jemba@ext.mpsa.com
 * @author pierre.thirouin@ext.mpsa.com
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Restriction {

    /**
     * The security expression related to the annotated element.
     * <p>
     * Please use an Expression Language.
     * <p>
     */
    String value() default "${false}";

    /**
     * The obfuscation handler to use in case the restriction is false.
     */
    Class<? extends DataObfuscationHandler<?>> obfuscation() default NullifyObfuscationHandler.class;
}
