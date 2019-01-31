/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core;

import javax.el.ELContext;
import javax.inject.Inject;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.seedstack.seed.core.fixtures.el.ELFixture;
import org.seedstack.seed.el.ELContextBuilder;
import org.seedstack.seed.el.ELService;
import org.seedstack.seed.testing.junit4.SeedITRunner;

@RunWith(SeedITRunner.class)
public class ExpressionLanguageIT {
    private static final String HELLO_WORLD = "hello world !";
    @Inject
    private ELService elService;
    @Inject
    private ELContextBuilder elContextBuilder;

    @Test
    public void evaluate_method_expression_with_the_service() {
        String userName = (String) elService.withExpression("${fixture.getName}", String.class)
                .withContext(elContextBuilder.defaultContext().withProperty("fixture", new ELFixture()).build())
                .asMethodExpression(new Class[]{}).invoke(new Object[]{});
        Assertions.assertThat(userName).isEqualTo("Jean Michel");

        String message = (String) elService.withExpression("${fixture.sayHello}", String.class)
                .withContext(elContextBuilder.defaultContext().withProperty("fixture", new ELFixture()).build())
                .asMethodExpression(new Class[]{String.class})
                .invoke(new Object[]{"hello"});
        Assertions.assertThat(message).isEqualTo(HELLO_WORLD);
    }

    @Test
    public void evaluate_value_expression_with_the_service() {
        Integer response = (Integer) elService.withExpression("${21*2}",
                Integer.class).withDefaultContext().asValueExpression().eval();
        Assertions.assertThat(response).isEqualTo(42);
    }

    @Test
    public void add_function_to_expression_language() throws NoSuchMethodException {
        Double response = (Double) elService.withExpression("${math:max(24,42)}", double.class)
                .withContext(elContextBuilder.defaultContext().withFunction("math", "max",
                        Math.class.getMethod("max", double.class, double.class)).build())
                .asValueExpression().eval();
        Assertions.assertThat(response).isEqualTo(42);
    }

    @Test
    public void check_boolean_eval_expression_language() {
        Boolean hasAuthorization = (Boolean) elService.withExpression("${falsy && falsy}", Boolean.class)
                .withContext(elContextBuilder.defaultContext().withProperty("truthy", true)
                        .withProperty("falsy", false).build())
                .asValueExpression().eval();
        Assertions.assertThat(hasAuthorization).isEqualTo(false);
    }

    @Test
    public void define_reusable_context() {
        ELContext elContext = elContextBuilder.defaultContext().withProperty("pok", HELLO_WORLD).build();
        Object eval = elService.withExpression("${pok}", String.class).withContext(
                elContext).asValueExpression().eval();
        Assertions.assertThat(eval).isEqualTo(HELLO_WORLD);
    }
}
