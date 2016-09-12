/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.el.internal;

import de.odysseus.el.util.SimpleContext;
import org.assertj.core.api.Assertions;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.seedstack.seed.it.SeedITRunner;

import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.MethodExpression;
import javax.el.ValueExpression;

/**
 * @author pierre.thirouin@ext.mpsa.com
 *         Date: 27/06/2014
 */
@RunWith(SeedITRunner.class)
public class Poc {

    private ELContext context = new SimpleContext();

    @Test
    public void evaluate_value_expression() {
        ExpressionFactory expressionFactory = ExpressionFactory.newInstance();
        ValueExpression valueExpression = expressionFactory.createValueExpression(context, "${2+3}", Integer.class);
        Integer value = (Integer) valueExpression.getValue(context);
        Assertions.assertThat(value).isEqualTo(5);
    }


    @Test
    public void evaluatevalue_expression_with_static_function() throws NoSuchMethodException {
        ExpressionFactory expressionFactory = ExpressionFactory.newInstance();
        SimpleContext simpleContext = new SimpleContext();
        simpleContext.setFunction("maths", "max", Math.class.getMethod("max", double.class, double.class));

        Integer firstArg = 2;
        Integer secondArg = 3;
        ValueExpression valueExpression = expressionFactory.createValueExpression(simpleContext, "${maths:max(firstArg,secondArg)}", Integer.class);
        simpleContext.getELResolver().setValue(simpleContext, null, "firstArg", firstArg);
        simpleContext.getELResolver().setValue(simpleContext, null, "secondArg", secondArg);

        Integer value = (Integer) valueExpression.getValue(simpleContext);
        Assertions.assertThat(value).isEqualTo(3);
    }

    @Test
    public void evaluate_method_expression() throws NoSuchMethodException {
        ExpressionFactory expressionFactory = ExpressionFactory.newInstance();
        SimpleContext simpleContext = new SimpleContext();

        MethodExpression methodExpression = expressionFactory.createMethodExpression(simpleContext, "${message.split}", String[].class, new Class[]{String.class});
        simpleContext.getELResolver().setValue(simpleContext, null, "message", "my long string");

        String[] value = (String[]) methodExpression.invoke(simpleContext, new Object[]{" "});
        String[] expected = new String[]{"my", "long", "string"};
        Assertions.assertThat(value).isEqualTo(expected);
    }

    @Test
    @Ignore("cannot test this without Java 8")
    public void evaluate_lambda_expression() throws NoSuchMethodException {
        ExpressionFactory expressionFactory = ExpressionFactory.newInstance();
        SimpleContext simpleContext = new SimpleContext();
        simpleContext.setFunction("maths", "max", Math.class.getMethod("max", double.class, double.class));

        Integer firstArg = 2;
        Integer secondArg = 3;
        ValueExpression valueExpression = expressionFactory.createValueExpression(simpleContext, "${maths:max((x -> x * 2)(arg[0]), arg[1])}", Integer.class);
        simpleContext.getELResolver().setValue(simpleContext, null, "arg", new Integer[]{2, 3});
        simpleContext.getELResolver().setValue(simpleContext, null, "secondArg", secondArg);

        Integer value = (Integer) valueExpression.getValue(simpleContext);
        Assertions.assertThat(value).isEqualTo(4);
    }
}
