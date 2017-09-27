/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core;

import com.google.inject.Injector;
import com.google.inject.Module;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.core.fixtures.el.Foo;
import org.seedstack.seed.core.fixtures.el.SomeDTO;
import org.seedstack.seed.core.rules.SeedITRule;

public class ExpressionLanguageHandlerIT {
    public static String message = "";
    public static Long count = 0L;
    @Rule
    public SeedITRule rule = new SeedITRule(this);
    private Injector injector;

    @Before
    public void before() {
        injector = rule.getKernel().objectGraph().as(Injector.class).createChildInjector((Module) binder -> {
            binder.bind(Foo.class);
            binder.bind(SomeDTO.class);
        });
    }

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
