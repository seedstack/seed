/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.el.internal;

import org.seedstack.seed.el.ELContextBuilder;
import org.seedstack.seed.el.ELService;
import org.seedstack.seed.it.SeedITRunner;
import de.odysseus.el.util.SimpleContext;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.inject.Inject;

/**
 * @author pierre.thirouin@ext.mpsa.com
 *         Date: 03/07/2014
 */
@RunWith(SeedITRunner.class)
public class ELServiceIT {

    public static final String HELLO_WORLD = "hello world !";
    @Inject
    private ELService elService;

    @Inject
    private ELContextBuilder elContextBuilder;

    public class User {
        public String getName() {
            return "Jean Michel";
        }

        public String sayHello(String message) {
            return message + " world !";
        }

        public boolean isAuthenticated = true;

        public boolean isNice = false;
    }

    @Test
    public void evaluate_method_expression_with_the_service() {
        String userName = (String) elService.withExpression("${user.getName}", String.class)
                .withContext(elContextBuilder.defaultContext().withProperty("user", new User()).build())
                .asMethodExpression(new Class[]{}).invoke(new Object[]{});

        Assertions.assertThat(userName).isEqualTo("Jean Michel");


        String message = (String) elService.withExpression("${user.sayHello}", String.class)
                .withContext(elContextBuilder.defaultContext().withProperty("user", new User()).build())
                .asMethodExpression(new Class[]{String.class})
                .invoke(new Object[]{"hello"});
        Assertions.assertThat(message).isEqualTo(HELLO_WORLD);
    }

    @Test
    public void evaluate_value_expression_with_the_service() {
        Integer response = (Integer) elService.withExpression("${21*2}", Integer.class).withDefaultContext().asValueExpression().eval();

        Assertions.assertThat(response).isEqualTo(42);
    }

    @Test
    public void add_function_to_expression_language() throws NoSuchMethodException {
        Double response = (Double) elService.withExpression("${math:max(24,42)}", double.class)
                .withContext(elContextBuilder.defaultContext().withFunction("math", "max", Math.class.getMethod("max", double.class, double.class)).build())
                .asValueExpression().eval();

        Assertions.assertThat(response).isEqualTo(42);
    }

    @Test
    public void check_boolean_eval_expression_language() throws NoSuchMethodException {
        Boolean hasAuthorization = (Boolean) elService.withExpression("${isAuthenticated && isNice}", Boolean.class)
                .withContext(elContextBuilder.defaultContext().withProperty("isAuthenticated", new User().isAuthenticated)
                        .withProperty("isNice", new User().isNice).build())
                .asValueExpression().eval();
        Assertions.assertThat(hasAuthorization).isEqualTo(false);
    }

    @Test
    public void use_custom_el_component() {
        ValueExpression valueExpression = ExpressionFactory.newInstance().createValueExpression(new SimpleContext(), "${13*2-4+20}", Integer.class);
        Integer result = (Integer) elService.withValueExpression(valueExpression).eval();
        Assertions.assertThat(result).isEqualTo(42);
    }

    @Test
    public void define_reusable_context() {
        ELContext elContext = elContextBuilder.defaultContext().withProperty("pok", HELLO_WORLD).build();
        Object eval = elService.withExpression("${pok}", String.class).withContext(elContext).asValueExpression().eval();
        Assertions.assertThat(eval).isEqualTo(HELLO_WORLD);
    }
}
