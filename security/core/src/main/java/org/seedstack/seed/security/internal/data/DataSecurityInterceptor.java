/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.internal.data;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.seedstack.seed.core.utils.SeedReflectionUtils;
import org.seedstack.seed.security.data.DataSecurityService;
import org.seedstack.seed.security.data.Secured;

import javax.inject.Inject;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import static org.seedstack.seed.core.utils.SeedReflectionUtils.isPresent;
import static org.seedstack.seed.core.utils.SeedReflectionUtils.methodsFromAncestors;

/**
 * This interceptor will apply Data Security Service on the annotated parameters and/or on the return value.
 * 
 * @author epo.jemba@ext.mpsa.com
 * @author pierre.thirouin@ext.mpsa.com
 *
 */
class DataSecurityInterceptor implements MethodInterceptor {

    @Inject
	private DataSecurityService dataSecurityService;
	
	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		
		Method candidate = invocation.getMethod();

        boolean returnToFilter = isPresent(methodsFromAncestors(candidate), Secured.class);

        Annotation[] parameterToSecured = SeedReflectionUtils.parameterAnnotationsFromAncestors(candidate, Secured.class);
		
		Object[] arguments = invocation.getArguments();
		Object o = invocation.proceed();
		
		if (returnToFilter) {
			dataSecurityService.secure(o);
		}
		
		for (int i = 0; parameterToSecured != null && i < parameterToSecured.length; i++) {
			if (parameterToSecured[i] != null) {
				dataSecurityService.secure(arguments[i]);
			}
		}
		
		return o;
	}

}
