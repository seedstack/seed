/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.internal.fixtures.data;


import org.seedstack.seed.security.data.Restriction;

/**
 *
 * 
 * @author epo.jemba@ext.mpsa.com
 *
 */
public class Dummy {
	
	
	@Restriction(value = "${false}" , obfuscation=DummyObfuscation.class )
	private String dummy1;
	
	@Restriction("${ hasRole('jedi') && hasPermission('academy:learn')  }")
	private Long dummy2;
	
	
	@Restriction("${ hasRole('jedi') && hasPermission('academy:learn')  }")
	private Boolean dummy3;
	
	
	@Restriction
	private String dummy4;
	
	
	public Dummy() {
	}
	
	public Dummy(String dummy1, Long dummy2, Boolean dummy3, String dummy4) {
		super();
		this.dummy1 = dummy1;
		this.dummy2 = dummy2;
		this.dummy3 = dummy3;
		this.dummy4 = dummy4;
	}
	
	public String getDummy1() {
		return dummy1;
	}
	
	public void setDummy1(String dummy1) {
		this.dummy1 = dummy1;
	}
	
	public Long getDummy2() {
		return dummy2;
	}
	
	public void setDummy2(Long dummy2) {
		this.dummy2 = dummy2;
	}
	
	public Boolean getDummy3() {
		return dummy3;
	}
	
	public void setDummy3(Boolean dummy3) {
		this.dummy3 = dummy3;
	}

	public String getDummy4() {
		return dummy4;
	}
	
}
