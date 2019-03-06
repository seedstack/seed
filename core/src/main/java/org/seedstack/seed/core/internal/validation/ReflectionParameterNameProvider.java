/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.validation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.ParameterNameProvider;

/**
 * Basic reflection-based parameter name provider.
 */
public class ReflectionParameterNameProvider implements ParameterNameProvider {
    @Override
    public List<String> getParameterNames(Constructor<?> constructor) {
        return collectParameterNames(constructor.getParameters());
    }

    @Override
    public List<String> getParameterNames(Method method) {
        return collectParameterNames(method.getParameters());
    }

    private List<String> collectParameterNames(Parameter[] parameters) {
        return Arrays.stream(parameters).map(Parameter::getName).collect(Collectors.toList());
    }
}
