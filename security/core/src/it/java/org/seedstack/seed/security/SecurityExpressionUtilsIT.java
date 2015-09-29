/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security;

import static org.assertj.core.api.Assertions.assertThat;
import java.lang.reflect.Method;

import javax.el.ELContext;
import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.seedstack.seed.el.api.ELContextBuilder;
import org.seedstack.seed.el.api.ELService;
import org.seedstack.seed.it.SeedITRunner;
import org.seedstack.seed.security.api.WithUser;
import org.seedstack.seed.security.internal.securityexpr.SecurityExpressionUtils;

/**
 *
 * 
 * @author epo.jemba@ext.mpsa.com
 * @author pierre.thirouin@ext.mpsa.com
 *
 */
@RunWith(SeedITRunner.class)
public class SecurityExpressionUtilsIT {

    @Inject
    private ELService elService;
    
    @Inject
    private ELContextBuilder elContextBuilder;
	
    @Test
    @WithUser(id = "Obiwan", password = "yodarulez")
    public void simple_el_security_integration_check() throws SecurityException, NoSuchMethodException {
    	assertThat( expression("${  hasRole('jedi') }") ).isTrue();
    }
    
    @Test
    @WithUser(id = "Anakin", password = "imsodark"   )
    public void composed_el_security_integration_check() throws SecurityException, NoSuchMethodException {
    	assertThat( expression("${ ! hasRole('jedi') && hasPermission('academy:learn')  }") ).isTrue();
    }
    
    private Boolean expression(String expression ) throws SecurityException, NoSuchMethodException {
    	Method m = SecurityExpressionUtils.class.getDeclaredMethod("hasRole" , String.class);
    	Method p = SecurityExpressionUtils.class.getDeclaredMethod("hasPermission" , String.class);
    	ELContext elContext = elContextBuilder.defaultContext()
    			.withFunction("", "hasRole", m )
    			.withFunction("", "hasPermission", p).build();
    	return (Boolean) elService.withExpression(expression, Boolean.class).withContext(elContext).asValueExpression().eval();
    }

}
