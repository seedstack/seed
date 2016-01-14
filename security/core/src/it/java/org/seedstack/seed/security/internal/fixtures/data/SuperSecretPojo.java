/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.internal.fixtures.data;

/**
 *
 * 
 * @author epo.jemba@ext.mpsa.com
 *
 */
public class SuperSecretPojo {

	@MyRestriction(expression = "${false}")
	private int myint = 123;
	@MyRestriction(expression = "${false}")
	private Integer myInteger = 123;
	@MyRestriction(expression = "${false}")
	private short myshort = 456;
	@MyRestriction(expression = "${false}")
	private Short myShort = 4554;
	@MyRestriction(expression = "${false}")
	private boolean myboolean = true;
	@MyRestriction(expression = "${false}")
	private Boolean myBoolean = Boolean.TRUE;
	@MyRestriction(expression = "${false}")
	private byte mybyte = 12;
	@MyRestriction(expression = "${false}")
	private Byte myByte = 123;
	@MyRestriction(expression = "${false}")
	private long mylong = 121l;
	@MyRestriction(expression = "${false}")
	private Long myLong = 45l;
	@MyRestriction(expression = "${false}")
	private float myfloat = 565f;
	@MyRestriction(expression = "${false}")
	private Float myFloat = 45456f;
	@MyRestriction(expression = "${false}")
	private double mydouble = 4545d;
	@MyRestriction(expression = "${false}")
	private Double myDouble = 545d;
	@MyRestriction(expression = "${false}")
	private char mycharacter = 'c';
	@MyRestriction(expression = "${false}")
	private Character myCharacter = ' ';
	@MyRestriction(expression = "${false}")
	private String myString = "zerzerrzet";
	
	public int getMyint() {
		return myint;
	}
	public Integer getMyInteger() {
		return myInteger;
	}
	public short getMyshort() {
		return myshort;
	}
	public Short getMyShort() {
		return myShort;
	}
	public boolean isMyboolean() {
		return myboolean;
	}
	public Boolean getMyBoolean() {
		return myBoolean;
	}
	public byte getMybyte() {
		return mybyte;
	}
	public Byte getMyByte() {
		return myByte;
	}
	public long getMylong() {
		return mylong;
	}
	public Long getMyLong() {
		return myLong;
	}
	public float getMyfloat() {
		return myfloat;
	}
	public Float getMyFloat() {
		return myFloat;
	}
	public double getMydouble() {
		return mydouble;
	}
	public Double getMyDouble() {
		return myDouble;
	}
	public char getMycharacter() {
		return mycharacter;
	}
	public Character getMyCharacter() {
		return myCharacter;
	}
	public String getMyString() {
		return myString;
	}
    
}
