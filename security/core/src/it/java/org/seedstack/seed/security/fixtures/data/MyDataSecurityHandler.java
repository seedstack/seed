/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.fixtures.data;

import org.seedstack.seed.security.spi.data.DataObfuscationHandler;
import org.seedstack.seed.security.spi.data.DataSecurityHandler;

/**
 *
 * 
 * @author epo.jemba@ext.mpsa.com
 *
 */
public class MyDataSecurityHandler implements DataSecurityHandler<MyRestriction> {

	@Override
	public Object securityExpression(MyRestriction annotation) {
		return annotation.expression();
	}

	@Override
	public Class<? extends DataObfuscationHandler<?>> securityObfuscationHandler(MyRestriction annotation) {

		if (annotation.todo() .equals( MyRestriction.Todo.Hide  )) {
			return Obfus.class;
		}
		if (annotation.todo() .equals( MyRestriction.Todo.Initial  )) {
			return InitialHandler.class;
		}
		return null;
	}
	
	
	public static class Obfus implements DataObfuscationHandler<Integer> {

		@Override
		public Integer obfuscate(Integer data) {
            Integer result = 0;
			if (data != null && data > 1000) {
            	result = (int) (Math.ceil(data / 1000) * 1000);
            }
			return result;
		}
		
		
	}
	
	public static class InitialHandler implements DataObfuscationHandler<String> {

		@Override
		public String obfuscate(String data) {
			String result = "";
			if (data != null && data.length() > 0) {
				result = data.charAt(0) + ".";
                result = result.toUpperCase();
			}
			return result;
		}
		
	}
	

}
