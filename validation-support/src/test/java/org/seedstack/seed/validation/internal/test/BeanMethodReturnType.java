/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.validation.internal.test;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.Range;

public class BeanMethodReturnType {
	    

	    private int age;
	    
	    private Long longNumber;
	    
	    private String name;
	    
	    private String firstName;
	    

	    @Range(min = 0, max = 200)
	    public int getAge()
	    {
	        return age;
	    }

	    public void setAge( int age)
	    {
	        this.age = age;
	    }

	    @NotNull(message = "Nom obligatoire")  @Size(min = 1, max = 10)
	    public String getName()
	    {
	        return name;
	    }

	    public void setName(
	    		 
	    		String name
	    		)
	    {
	        this.name = name;
	    }

	    @NotNull(groups = { Groupe1Checks.class })
	    public String getFirstName()
	    {
	        return firstName;
	    }

	    public void setFirstName ( 
	    		
	    		String firstName )
	    {
	        this.firstName = firstName;
	    }

	    @NotNull @Min(value = 10L) @Max(value = 999999999999999999L)
	    public Long getLongNumber()
	    {
	        return longNumber;
	    }

	    public void setLongNumber( Long longNumber )
	    {
	        this.longNumber = longNumber;
	    }

}
