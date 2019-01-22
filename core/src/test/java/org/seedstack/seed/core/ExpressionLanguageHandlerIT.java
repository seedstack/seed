/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core;

import com.google.inject.Injector;
import javax.inject.Inject;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.core.fixtures.el.Foo;
import org.seedstack.seed.core.fixtures.el.SomeDTO;
import org.seedstack.seed.testing.junit4.SeedITRunner;

@RunWith(SeedITRunner.class)
public class ExpressionLanguageHandlerIT {
    public static String message = "";
    public static Long count = 0L;
    @Inject
    private Injector injector;

    @Test
    public void el_was_coerced_in_long() {
        injector.getInstance(SomeDTO.class).setMessage("hello");
        Assertions.assertThat(count).isEqualTo(4);
    }

    @Test(expected = SeedException.class)
    public void error_when_annotation_value_not_available() {
        injector.getInstance(Foo.class).pok();
    }

    @Test(expected = SeedException.class)
    public void failing_el_evaluation_throw_seed_exception() {
        injector.getInstance(SomeDTO.class).failingMethod();
    }
}
