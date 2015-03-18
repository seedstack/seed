/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.api;

import org.junit.Test;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;

public class SeedExceptionTest {


	static enum TotoErrorCode implements ErrorCode
	{
		JOKE_MODE , CARAMBAR_MODE ;
	}

	static class TotoException extends SeedException
	{
		private static final long serialVersionUID = 1L;

		public TotoException(ErrorCode errorCode) {
			super(errorCode);
		}

		public TotoException(ErrorCode errorCode, Throwable throwable) {
			super(errorCode, throwable);
		}
	}

	@Test(expected=TotoException.class)
	public void testCreateNewTotoException1()
	{
		TotoException te = SeedException.createNew(TotoException.class,TotoErrorCode.CARAMBAR_MODE)
				.put("key1", "value1")
		        .put("key2", "value2");
		throw te;
	}

	@Test(expected=SeedException.class)
	public void wrap_Should_Work_Fine ()
	{
		try
		{
			throw new NullPointerException();
		}
		catch (Exception exception)
		{
			SeedException.wrap(exception, TotoErrorCode.JOKE_MODE)
			  .put("Error Code", "this is how we do it !")
			  .thenThrows();
		}
	}

	@Test(expected = TotoException.class)
	public void wrap_Should_Work_Fine_WIth_Descendant ()
	{
		try
		{
			throw new NullPointerException();
		}
		catch (Exception exception)
		{
			SeedException.wrap ( TotoException.class , exception, TotoErrorCode.JOKE_MODE )
			.put("Error Code", "this is how we do it !")
			.thenThrows();
		}
	}

	@Test(expected = SeedException.class)
	public void wrap_Should_Work_Fine_WIth_Change_Of_ErrorCode(){
		try{
			throw new TotoException(TotoErrorCode.JOKE_MODE);
		}catch (TotoException e) {
			SeedException.wrap(TotoException.class, e, TotoErrorCode.CARAMBAR_MODE).thenThrows();
		}
	}

	@Test(expected=SeedException.class)
	public void wrap_Should_Work_Fine_1 () {
		try{
			throw new TotoException(TotoErrorCode.JOKE_MODE);
		}catch (TotoException exception){
			SeedException.wrap(exception,TotoErrorCode.JOKE_MODE)
			  .put("Error Code", "this is how we do it !")
			  .thenThrows();
		}
	}

	@Test(expected=SeedException.class)
	public void wrap_Should_Work_Fine_2 () {
		try{
			throw new TotoException(TotoErrorCode.JOKE_MODE);
		}catch (TotoException exception){
			SeedException.wrap(exception,TotoErrorCode.CARAMBAR_MODE)
			  .put("Error Code", "this is how we do it !")
			  .thenThrows();
		}
	}

	@Test
	public void multiple_causes_should_be_visible() throws Exception {
		StringWriter stringWriter = new StringWriter();
		SeedException.wrap(SeedException.wrap(new RuntimeException("yop"), TotoErrorCode.CARAMBAR_MODE), TotoErrorCode.JOKE_MODE).printStackTrace(new PrintWriter(stringWriter));
		String text = stringWriter.toString();

		assertThat(text).contains("Caused by: java.lang.RuntimeException: yop");
		assertThat(text).contains("Caused by: org.seedstack.seed.core.api.SeedException: CARAMBAR_MODE (TotoErrorCode)");
		assertThat(text).contains("org.seedstack.seed.core.api.SeedException: JOKE_MODE (TotoErrorCode)");
	}
}
