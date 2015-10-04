/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.spi.data;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author epo.jemba@ext.mpsa.com
 */
public class NullifyObfuscationHandlerTest {


    static class Foo {

    }

    private int myint = 123;
    private Integer myInteger = 123;
    private short myshort = 456;
    private Short myShort = 4554;
    private boolean myboolean = true;
    private Boolean myBoolean = Boolean.TRUE;
    private byte mybyte = 12;
    private Byte myByte = 123;
    private long mylong = 121l;
    private Long myLong = 45l;
    private float myfloat = 565f;
    private Float myFloat = 45456f;
    private double mydouble = 4545d;
    private Double myDouble = 545d;
    private char mycharacter = 'c';
    private Character myCharacter = ' ';
    private String myString = "zerzerrzet";
    private Object dummy = new Object();
    private Foo foo = new Foo();

    NullifyObfuscationHandler underTest;

    @Before
    public void init() {
        underTest = new NullifyObfuscationHandler();
    }

    @Test
    public void testObfuscate() {

        assertThat(underTest.obfuscate(getMyint())).isEqualTo(0);
        assertThat(underTest.obfuscate(getMyInteger())).isEqualTo(0);

        assertThat(underTest.obfuscate(getMyshort())).isEqualTo((short) 0);
        assertThat(underTest.obfuscate(getMyShort())).isEqualTo((short) 0);

        assertThat((Boolean) underTest.obfuscate(isMyboolean())).isFalse();
        assertThat(underTest.obfuscate(getMyBoolean())).isEqualTo(new Boolean(false));

        assertThat(underTest.obfuscate(getMybyte())).isEqualTo((byte) 0);
        assertThat(underTest.obfuscate(getMyByte())).isEqualTo((byte) 0);

        assertThat(underTest.obfuscate(getMylong())).isEqualTo(0l);
        assertThat(underTest.obfuscate(getMyLong())).isEqualTo(0l);

        assertThat(underTest.obfuscate(getMyfloat())).isEqualTo(0f);
        assertThat(underTest.obfuscate(getMyFloat())).isEqualTo(0f);

        assertThat(underTest.obfuscate(getMydouble())).isEqualTo(0d);
        assertThat(underTest.obfuscate(getMyDouble())).isEqualTo(0d);

        assertThat(underTest.obfuscate(getMycharacter())).isEqualTo((char) 0);
        assertThat(underTest.obfuscate(getMyCharacter())).isEqualTo((char) 0);

        assertThat(underTest.obfuscate(getMyString())).isEqualTo("");


        assertThat(underTest.obfuscate(dummy)).isNull();
        assertThat(underTest.obfuscate(foo)).isNull();


    }


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
