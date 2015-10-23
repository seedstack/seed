/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.fixtures.data;

import org.seedstack.seed.security.fixtures.data.MyRestriction.Todo;

/**
 *
 * 
 * @author epo.jemba@ext.mpsa.com
 *
 */
public class MyUnsecuredPojo {
	
	private String firstname;
	
	@MyRestriction(expression="${1 == 2}" , todo=Todo.Initial)
	private String name;
	
	@MyRestriction(expression="${ hasRole('jedi') && hasPermission('academy:learn')  }" , todo=Todo.Hide)
	private Integer salary;

	// TODO check when no Obfuscation Handler is given
	@MyRestriction(expression="${false}")
	private String password;


	public MyUnsecuredPojo(String name, String firstname, String password , Integer salary) {
		this.name = name;
		this.firstname = firstname;
		this.password = password;
		this.salary = salary;
		
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getFirstname() {
		return firstname;
	}
	
	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}
	
	public Integer getSalary() {
		return salary;
	}
	
	public void setSalary(Integer salary) {
		this.salary = salary;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	

}
