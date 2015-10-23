/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.application;

import org.seedstack.seed.core.api.Configuration;
import org.seedstack.seed.core.api.SeedException;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.Fail;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConfigurationMembersInjectorTest {

	@Configuration(value = "testConfigString", mandatory = false)
	String testConfigString;

	@Configuration(value = "testConfigStringArray", mandatory = false)
	String[] testConfigStringArray;

	@Configuration(value = "testConfigBoolean", mandatory = false)
	Boolean testConfigBoolean;

	@Configuration(value = "testConfigBooleanArray", mandatory = false)
	Boolean[] testConfigBooleanArray;

	@Configuration(value = "testConfigboolean", mandatory = false)
	boolean testConfigboolean;

	@Configuration(value = "testConfigbooleanArray", mandatory = false)
	boolean[] testConfigbooleanArray;

	@Configuration(value = "testConfigLong", mandatory = false)
	Long testConfigLong;

	@Configuration(value = "testConfigLongArray", mandatory = false)
	Long[] testConfigLongArray;

	@Configuration(value = "testConfiglong", mandatory = false)
	long testConfiglong;

	@Configuration(value = "testConfiglongArray", mandatory = false)
	long[] testConfiglongArray;

	@Configuration(value = "testConfigInteger", mandatory = false)
	Integer testConfigInteger;

	@Configuration(value = "testConfigIntegerArray", mandatory = false)
	Integer[] testConfigIntegerArray;

	@Configuration(value = "testConfigint", mandatory = false)
	int testConfigint;

	@Configuration(value = "testConfigintArray", mandatory = false)
	int[] testConfigintArray;

	@Configuration(value = "testConfigDouble", mandatory = false)
	Double testConfigDouble;

	@Configuration(value = "testConfigDoubleArray", mandatory = false)
	Double[] testConfigDoubleArray;

	@Configuration(value = "testConfigdouble", mandatory = false)
	double testConfigdouble;

	@Configuration(value = "testConfigdoubleArray", mandatory = false)
	double[] testConfigdoubleArray;

	@Configuration(value = "testConfigFloat", mandatory = false)
	Float testConfigFloat;

	@Configuration(value = "testConfigFloatArray", mandatory = false)
	Float[] testConfigFloatArray;

	@Configuration(value = "testConfigfloat", mandatory = false)
	float testConfigfloat;

	@Configuration(value = "testConfigfloatArray", mandatory = false)
	float[] testConfigfloatArray;

	@Configuration(value = "testConfigByte", mandatory = false)
	Byte testConfigByte;

	@Configuration(value = "testConfigByteArray", mandatory = false)
	Byte[] testConfigByteArray;

	@Configuration(value = "testConfigbyte", mandatory = false)
	byte testConfigbyte;

	@Configuration(value = "testConfigbyteArray", mandatory = false)
	byte[] testConfigbyteArray;

	@Configuration(value = "testConfigCharacter", mandatory = false)
	Character testConfigCharacter;

	@Configuration(value = "testConfigCharacterArray", mandatory = false)
	Character[] testConfigCharacterArray;

	@Configuration(value = "testConfigchar", mandatory = false)
	char testConfigchar;

	@Configuration(value = "testConfigcharArray", mandatory = false)
	char[] testConfigcharArray;

	@Configuration(value = "testConfigShort", mandatory = false)
	Short testConfigShort;

	@Configuration(value = "testConfigShortArray", mandatory = false)
	Short[] testConfigShortArray;

	@Configuration(value = "testConfigshort", mandatory = false)
	short testConfigshort;

	@Configuration(value = "testConfigshortArray", mandatory = false)
	short[] testConfigshortArray;

	@Configuration(value = "")
	String testEmptyValue;

	@Configuration(value = "testMandatoryValue", mandatory = true)
	String testMandatoryValue;

	@Test(expected = SeedException.class)
	public void injectorConfigForMandatoryValueTest() {
        injectMembersTest("testMandatoryValue",false);
	}

	@Test(expected = SeedException.class)
	public void injectorConfigForEmptyValueTest() {
		injectMembersTest("testEmptyValue",true);
	}

	@Test
	public void injectorConfigshortTest() {
        injectMembersTest("testConfigshort", false);
        Assertions.assertThat(testConfigshort).isNotEqualTo(Short.MAX_VALUE);
		injectMembersTest("testConfigshort", true);
        Assertions.assertThat(testConfigshort).isEqualTo(Short.MAX_VALUE);
    }

	@Test
	public void injectorConfigshortArrayTest() {
        injectMembersTest("testConfigshortArray", false);
        Assertions.assertThat(testConfigshortArray).isNull();
		injectMembersTest("testConfigshortArray", true);
        Assertions.assertThat(testConfigshortArray).isNotNull();
        Assertions.assertThat(testConfigshortArray.length).isEqualTo(1);
        Assertions.assertThat(testConfigshortArray[0]).isEqualTo(Short.MAX_VALUE);
    }

	@Test
	public void injectorConfigShortTest() {
        injectMembersTest("testConfigShort", false);
        Assertions.assertThat(testConfigShort).isNull();
		injectMembersTest("testConfigShort",true);
		Assertions.assertThat(testConfigShort).isNotNull();
		Assertions.assertThat(testConfigShort).isEqualTo(Short.MAX_VALUE);
	}

	@Test
	public void injectorConfigShortArrayTest() {
        injectMembersTest("testConfigShortArray", false);
        Assertions.assertThat(testConfigShortArray).isNull();
		injectMembersTest("testConfigShortArray",true);
		Assertions.assertThat(testConfigShortArray).isNotNull();
		Assertions.assertThat(testConfigShortArray.length).isEqualTo(1);
		Assertions.assertThat(testConfigShortArray[0]).isEqualTo(Short.MAX_VALUE);
	}

	@Test
	public void injectorConfigcharTest() {
        injectMembersTest("testConfigchar", false);
        Assertions.assertThat(testConfigchar).isNotEqualTo("t".charAt(0));
		injectMembersTest("testConfigchar", true);
		Assertions.assertThat(testConfigchar).isEqualTo("t".charAt(0));
	}

	@Test
	public void injectorConfigcharArrayTest() {
        injectMembersTest("testConfigcharArray", false);
        Assertions.assertThat(testConfigcharArray).isNull();
		injectMembersTest("testConfigcharArray", true);
		Assertions.assertThat(testConfigcharArray).isNotNull();
		Assertions.assertThat(testConfigcharArray.length).isEqualTo(1);
		Assertions.assertThat(testConfigcharArray[0]).isEqualTo("t".charAt(0));
	}

	@Test
	public void injectorConfigCharacterTest() {
        injectMembersTest("testConfigCharacter", false);
        Assertions.assertThat(testConfigCharacter).isNull();
		injectMembersTest("testConfigCharacter", true);
		Assertions.assertThat(testConfigCharacter).isNotNull();
		Assertions.assertThat(testConfigCharacter).isEqualTo("t".charAt(0));
    }

	@Test
	public void injectorConfigCharacterArrayTest() {
        injectMembersTest("testConfigCharacterArray", false);
        Assertions.assertThat(testConfigCharacterArray).isNull();
		injectMembersTest("testConfigCharacterArray", true);
		Assertions.assertThat(testConfigCharacterArray).isNotNull();
		Assertions.assertThat(testConfigCharacterArray.length).isEqualTo(1);
		Assertions.assertThat(testConfigCharacterArray[0]).isEqualTo("t".charAt(0));
	}

	@Test
	public void injectorConfigbyteTest() {
		injectMembersTest("testConfigbyte", false);
		Assertions.assertThat(testConfigbyte).isNotEqualTo(Byte.MAX_VALUE);
        injectMembersTest("testConfigbyte", true);
        Assertions.assertThat(testConfigbyte).isEqualTo(Byte.MAX_VALUE);
	}

	@Test
	public void injectorConfigbyteArrayTest() {
		injectMembersTest("testConfigbyteArray", false);
		Assertions.assertThat(testConfigbyteArray).isNull();
        injectMembersTest("testConfigbyteArray", true);
        Assertions.assertThat(testConfigbyteArray).isNotNull();
        Assertions.assertThat(testConfigbyteArray.length).isEqualTo(1);
        Assertions.assertThat(testConfigbyteArray[0]).isEqualTo(Byte.MAX_VALUE);
	}

	@Test
	public void injectorConfigByteTest() {
		injectMembersTest("testConfigByte", false);
		Assertions.assertThat(testConfigByte).isNull();
        injectMembersTest("testConfigByte", true);
        Assertions.assertThat(testConfigByte).isNotNull();
        Assertions.assertThat(testConfigByte).isEqualTo(Byte.MAX_VALUE);
	}

	@Test
	public void injectorConfigByteArrayTest() {
		injectMembersTest("testConfigByteArray", false);
		Assertions.assertThat(testConfigByteArray).isNull();
        injectMembersTest("testConfigByteArray", true);
        Assertions.assertThat(testConfigByteArray).isNotNull();
        Assertions.assertThat(testConfigByteArray.length).isEqualTo(1);
        Assertions.assertThat(testConfigByteArray[0]).isEqualTo(Byte.MAX_VALUE);
	}

	@Test
	public void injectorConfigfloatTest() {
		injectMembersTest("testConfigfloat", false);
		Assertions.assertThat(testConfigfloat).isNotEqualTo(Float.MAX_VALUE);
        injectMembersTest("testConfigfloat", true);
        Assertions.assertThat(testConfigfloat).isEqualTo(Float.MAX_VALUE);
	}

	@Test
	public void injectorConfigfloatArrayTest() {
		injectMembersTest("testConfigfloatArray", false);
		Assertions.assertThat(testConfigfloatArray).isNull();
        injectMembersTest("testConfigfloatArray", true);
        Assertions.assertThat(testConfigfloatArray).isNotNull();
        Assertions.assertThat(testConfigfloatArray.length).isEqualTo(1);
        Assertions.assertThat(testConfigfloatArray[0]).isEqualTo(Float.MAX_VALUE);
	}

	@Test
	public void injectorConfigFloatTest() {
		injectMembersTest("testConfigFloat", false);
		Assertions.assertThat(testConfigFloat).isNull();
        injectMembersTest("testConfigFloat", true);
        Assertions.assertThat(testConfigFloat).isNotNull();
        Assertions.assertThat(testConfigFloat).isEqualTo(Float.MAX_VALUE);
	}

	@Test
	public void injectorConfigFloatArrayTest() {
		injectMembersTest("testConfigFloatArray", false);
		Assertions.assertThat(testConfigFloatArray).isNull();
        injectMembersTest("testConfigFloatArray", true);
        Assertions.assertThat(testConfigFloatArray).isNotNull();
        Assertions.assertThat(testConfigFloatArray.length).isEqualTo(1);
        Assertions.assertThat(testConfigFloatArray[0]).isEqualTo(Float.MAX_VALUE);
	}

	@Test
	public void injectorConfigdoubleTest() {
		injectMembersTest("testConfigdouble", false);
		Assertions.assertThat(testConfigdouble).isNotEqualTo(Double.MAX_VALUE);
        injectMembersTest("testConfigdouble", true);
        Assertions.assertThat(testConfigdouble).isEqualTo(Double.MAX_VALUE);
	}

	@Test
	public void injectorConfigdoubleArrayTest() {
		injectMembersTest("testConfigdoubleArray", false);
		Assertions.assertThat(testConfigdoubleArray).isNull();
        injectMembersTest("testConfigdoubleArray", true);
        Assertions.assertThat(testConfigdoubleArray).isNotNull();
        Assertions.assertThat(testConfigdoubleArray.length).isEqualTo(1);
        Assertions.assertThat(testConfigdoubleArray[0]).isEqualTo(Double.MAX_VALUE);
	}

	@Test
	public void injectorConfigDoubleTest() {
		injectMembersTest("testConfigDouble", false);
		Assertions.assertThat(testConfigDouble).isNull();
        injectMembersTest("testConfigDouble", true);
        Assertions.assertThat(testConfigDouble).isNotNull();
        Assertions.assertThat(testConfigDouble).isEqualTo(Double.MAX_VALUE);
	}

	@Test
	public void injectorConfigDoubleArrayTest() {
		injectMembersTest("testConfigDoubleArray", false);
		Assertions.assertThat(testConfigDoubleArray).isNull();
        injectMembersTest("testConfigDoubleArray", true);
        Assertions.assertThat(testConfigDoubleArray).isNotNull();
        Assertions.assertThat(testConfigDoubleArray.length).isEqualTo(1);
        Assertions.assertThat(testConfigDoubleArray[0]).isEqualTo(Double.MAX_VALUE);
	}

	@Test
	public void injectorConfigintTest() {
		injectMembersTest("testConfigint", false);
		Assertions.assertThat(testConfigint).isNotEqualTo(Integer.MAX_VALUE);
        injectMembersTest("testConfigint", true);
        Assertions.assertThat(testConfigint).isEqualTo(Integer.MAX_VALUE);
	}

	@Test
	public void injectorConfigintArrayTest() {
		injectMembersTest("testConfigintArray", false);
		Assertions.assertThat(testConfigintArray).isNull();
        injectMembersTest("testConfigintArray", true);
        Assertions.assertThat(testConfigintArray).isNotNull();
        Assertions.assertThat(testConfigintArray.length).isEqualTo(1);
        Assertions.assertThat(testConfigintArray[0]).isEqualTo(Integer.MAX_VALUE);
	}

	@Test
	public void injectorConfigIntegerTest() {
		injectMembersTest("testConfigInteger", false);
		Assertions.assertThat(testConfigInteger).isNull();
        injectMembersTest("testConfigInteger", true);
        Assertions.assertThat(testConfigInteger).isNotNull();
        Assertions.assertThat(testConfigInteger).isEqualTo(Integer.MAX_VALUE);
	}

	@Test
	public void injectorConfigIntegerArrayTest() {
		injectMembersTest("testConfigIntegerArray", false);
		Assertions.assertThat(testConfigIntegerArray).isNull();
        injectMembersTest("testConfigIntegerArray", true);
        Assertions.assertThat(testConfigIntegerArray).isNotNull();
        Assertions.assertThat(testConfigIntegerArray.length).isEqualTo(1);
        Assertions.assertThat(testConfigIntegerArray[0]).isEqualTo(Integer.MAX_VALUE);
	}

	@Test
	public void injectorConfiglongTest() {
		injectMembersTest("testConfiglong", false);
		Assertions.assertThat(testConfiglong).isNotEqualTo(Long.MAX_VALUE);
        injectMembersTest("testConfiglong", true);
        Assertions.assertThat(testConfiglong).isEqualTo(Long.MAX_VALUE);
	}

	@Test
	public void injectorConfiglongArrayTest() {
		injectMembersTest("testConfiglongArray", false);
		Assertions.assertThat(testConfiglongArray).isNull();
        injectMembersTest("testConfiglongArray", true);
        Assertions.assertThat(testConfiglongArray).isNotNull();
        Assertions.assertThat(testConfiglongArray.length).isEqualTo(1);
        Assertions.assertThat(testConfiglongArray[0]).isEqualTo(Long.MAX_VALUE);
	}

	@Test
	public void injectorConfigLongTest() {
		injectMembersTest("testConfigLong", false);
		Assertions.assertThat(testConfigLong).isNull();
        injectMembersTest("testConfigLong", true);
        Assertions.assertThat(testConfigLong).isNotNull();
        Assertions.assertThat(testConfigLong).isEqualTo(Long.MAX_VALUE);
	}

	@Test
	public void injectorConfigLongArrayTest() {
		injectMembersTest("testConfigLongArray", false);
		Assertions.assertThat(testConfigLongArray).isNull();
        injectMembersTest("testConfigLongArray", true);
        Assertions.assertThat(testConfigLongArray).isNotNull();
        Assertions.assertThat(testConfigLongArray.length).isEqualTo(1);
        Assertions.assertThat(testConfigLongArray[0]).isEqualTo(Long.MAX_VALUE);
	}

	@Test
	public void injectorConfigbooleanTest() {
		injectMembersTest("testConfigboolean", false);
		Assertions.assertThat(testConfigboolean).isNotEqualTo(true);
        injectMembersTest("testConfigboolean", true);
        Assertions.assertThat(testConfigboolean).isEqualTo(true);
	}

	@Test
	public void injectorConfigbooleanArrayTest() {
		injectMembersTest("testConfigbooleanArray", false);
		Assertions.assertThat(testConfigbooleanArray).isNull();
        injectMembersTest("testConfigbooleanArray", true);
        Assertions.assertThat(testConfigbooleanArray).isNotNull();
        Assertions.assertThat(testConfigbooleanArray.length).isEqualTo(1);
        Assertions.assertThat(testConfigbooleanArray[0]).isEqualTo(true);
	}

	@Test
	public void injectorConfigBooleanTest() {
		injectMembersTest("testConfigBoolean", false);
		Assertions.assertThat(testConfigBoolean).isNull();
        injectMembersTest("testConfigBoolean", true);
        Assertions.assertThat(testConfigBoolean).isNotNull();
        Assertions.assertThat(testConfigBoolean).isEqualTo(true);
	}

	@Test
	public void injectorConfigBooleanArrayTest() {
		injectMembersTest("testConfigBooleanArray", false);
		Assertions.assertThat(testConfigBooleanArray).isNull();
        injectMembersTest("testConfigBooleanArray", true);
        Assertions.assertThat(testConfigBooleanArray).isNotNull();
        Assertions.assertThat(testConfigBooleanArray.length).isEqualTo(1);
        Assertions.assertThat(testConfigBooleanArray[0]).isEqualTo(true);
	}

	@Test
	public void injectorConfigStringTest() {
		injectMembersTest("testConfigString", false);
		Assertions.assertThat(testConfigString).isNull();
        injectMembersTest("testConfigString", true);
        Assertions.assertThat(testConfigString).isNotNull();
        Assertions.assertThat(testConfigString).isEqualTo("test");
	}

	@Test
	public void injectorConfigStringArrayTest() {
		injectMembersTest("testConfigStringArray", false);
		Assertions.assertThat(testConfigStringArray).isNull();
        injectMembersTest("testConfigStringArray", true);
        Assertions.assertThat(testConfigStringArray).isNotNull();
        Assertions.assertThat(testConfigStringArray.length).isEqualTo(1);
        Assertions.assertThat(testConfigStringArray[0]).isEqualTo("test");
	}

	public void injectMembersTest(String configParamToTest, boolean isInConfig) {
		Field field;
		try {
			field = this.getClass().getDeclaredField(configParamToTest);
			ConfigurationMembersInjector<ConfigurationMembersInjectorTest> configurationMembersInjector = new ConfigurationMembersInjector<ConfigurationMembersInjectorTest>(
					field, mockConfiguration(field,isInConfig), field.getAnnotation(Configuration.class));
			configurationMembersInjector.injectMembers(this);
		} catch (SecurityException e) {
			Fail.fail(e.getMessage());
		} catch (NoSuchFieldException e) {
			Fail.fail(e.getMessage());
		}
	}


	public org.apache.commons.configuration.Configuration mockConfiguration(Field field, boolean isInconfig) {
		if (field == null) {
			return null;
		}
		org.apache.commons.configuration.Configuration configuration = mock(org.apache.commons.configuration.Configuration.class);
		when(configuration.containsKey(field.getName())).thenReturn(isInconfig);
		if(isInconfig){
			Class<?> type = field.getType();
			String configParamName = field.getAnnotation(Configuration.class).value();
            if (type.isArray()) {
                type = type.getComponentType();

                if (type == Integer.TYPE || type == Integer.class) {
                    when(configuration.getStringArray(configParamName)).thenReturn(new String[] { String.valueOf(Integer.MAX_VALUE) });
                }else if (type == Boolean.TYPE || type == Boolean.class) {
                    when(configuration.getStringArray(configParamName)).thenReturn(new String[] { "true" });
                } else if (type == Short.TYPE || type == Short.class) {
                    when(configuration.getStringArray(configParamName)).thenReturn(new String[] { String.valueOf(Short.MAX_VALUE) });
                } else if (type == Byte.TYPE || type == Byte.class) {
                    when(configuration.getStringArray(configParamName)).thenReturn(new String[] { String.valueOf(Byte.MAX_VALUE) });
                } else if (type == Long.TYPE || type == Long.class) {
                    when(configuration.getStringArray(configParamName)).thenReturn(new String[] { String.valueOf(Long.MAX_VALUE) });
                } else if (type == Float.TYPE || type == Float.class) {
                    when(configuration.getStringArray(configParamName)).thenReturn(new String[] { String.valueOf(Float.MAX_VALUE) });
                } else if (type == Double.TYPE || type == Double.class) {
                    when(configuration.getStringArray(configParamName)).thenReturn(new String[] { String.valueOf(Double.MAX_VALUE) });
                } else if (type == Character.TYPE || type == Character.class) {
                    when(configuration.getStringArray(configParamName)).thenReturn(new String[] { "t" });
                } else if (type == String.class) {
                    when(configuration.getStringArray(configParamName)).thenReturn(new String[] { "test" });
                } else
                    throw new IllegalArgumentException("Type " + type + " cannot be mocked");
            } else {
                if (type == Integer.TYPE || type == Integer.class) {
                    when(configuration.getString(configParamName)).thenReturn(String.valueOf(Integer.MAX_VALUE));
                }else if (type == Boolean.TYPE || type == Boolean.class) {
                    when(configuration.getString(configParamName)).thenReturn("true");
                } else if (type == Short.TYPE || type == Short.class) {
                    when(configuration.getString(configParamName)).thenReturn(String.valueOf(Short.MAX_VALUE));
                } else if (type == Byte.TYPE || type == Byte.class) {
                    when(configuration.getString(configParamName)).thenReturn(String.valueOf(Byte.MAX_VALUE));
                } else if (type == Long.TYPE || type == Long.class) {
                    when(configuration.getString(configParamName)).thenReturn(String.valueOf(Long.MAX_VALUE));
                } else if (type == Float.TYPE || type == Float.class) {
                    when(configuration.getString(configParamName)).thenReturn(String.valueOf(Float.MAX_VALUE));
                } else if (type == Double.TYPE || type == Double.class) {
                    when(configuration.getString(configParamName)).thenReturn(String.valueOf(Double.MAX_VALUE));
                } else if (type == Character.TYPE || type == Character.class) {
                    when(configuration.getString(configParamName)).thenReturn("t");
                } else if (type == String.class) {
                    when(configuration.getString(configParamName)).thenReturn("test");
                } else
                    throw new IllegalArgumentException("Type " + type + " cannot be mocked");
            }
		}
		return configuration;
	}

}
