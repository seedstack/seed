/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.validation;

import java.util.Locale;
import javax.el.ExpressionFactory;
import org.hibernate.validator.internal.engine.messageinterpolation.InterpolationTerm;
import org.hibernate.validator.internal.engine.messageinterpolation.ParameterTermResolver;
import org.hibernate.validator.messageinterpolation.AbstractMessageInterpolator;
import org.seedstack.seed.core.internal.el.ELPlugin;
import org.slf4j.Logger;

public class SeedMessageInterpolator extends AbstractMessageInterpolator {
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(SeedMessageInterpolator.class);

    @Override
    public String interpolate(Context context, Locale locale, String term) {
        if (InterpolationTerm.isElExpression(term)) {
            if (ELPlugin.isEnabled()) {
                InterpolationTerm expression = new InterpolationTerm(
                        term,
                        locale,
                        (ExpressionFactory) ELPlugin.getExpressionFactory());
                return expression.interpolate(context);
            } else {
                LOGGER.warn("Message contains EL expression: {}, which is not available in the runtime environment",
                        term);
                return term;
            }
        } else {
            ParameterTermResolver parameterTermResolver = new ParameterTermResolver();
            return parameterTermResolver.interpolate(context, term);
        }
    }
}
