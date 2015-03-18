/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed;


import org.seedstack.seed.core.api.Configuration;
import org.seedstack.seed.it.AbstractSeedIT;
import org.seedstack.wsdl.seed.calculator.CalculatorService;
import org.seedstack.wsdl.seed.calculator.CalculatorWS;
import org.seedstack.wsdl.seed.calculator.ImaginaryNumber;
import com.sun.xml.ws.client.ClientTransportException;
import com.sun.xml.ws.fault.ServerSOAPFaultException;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import javax.xml.ws.BindingProvider;

import static org.junit.Assert.fail;

public class WSStandaloneIT extends AbstractSeedIT {
    @Configuration("org.seedstack.seed.ws.test-port")
    private int wsPort;

    @Test(expected = ServerSOAPFaultException.class)
    public void without_security() {
        CalculatorService calculatorService = new CalculatorService();
        CalculatorWS calculatorWS = calculatorService.getCalculatorSoapPort();
        ((BindingProvider) calculatorWS).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, "http://localhost:" + wsPort + "/ws/calculator");
        calculatorWS.add(1, 1);
        fail("should have failed since access is denied");
    }

    @Test
    public void with_complex_types() {
        CalculatorService calculatorService = new CalculatorService();
        CalculatorWS calculatorWS = calculatorService.getCalculatorSoapPort();
        ((BindingProvider) calculatorWS).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, "http://localhost:" + wsPort + "/ws/calculator");
        ((BindingProvider) calculatorWS).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, "limited");
        ((BindingProvider) calculatorWS).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, "good");

        ImaginaryNumber imaginaryNumber1 = new ImaginaryNumber();
        imaginaryNumber1.setReal(1);
        imaginaryNumber1.setImaginary(1);

        ImaginaryNumber imaginaryNumber2 = new ImaginaryNumber();
        imaginaryNumber2.setReal(1);
        imaginaryNumber2.setImaginary(1);

        ImaginaryNumber result = calculatorWS.addImaginary(imaginaryNumber1, imaginaryNumber2);
        Assertions.assertThat(result.getReal()).isEqualTo(2);
        Assertions.assertThat(result.getImaginary()).isEqualTo(2);
    }

    @Test
    public void limited_valid_user_account_calling_allowed_method() {
        CalculatorService calculatorService = new CalculatorService();
        CalculatorWS calculatorWS = calculatorService.getCalculatorSoapPort();
        ((BindingProvider) calculatorWS).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, "http://localhost:" + wsPort + "/ws/calculator");
        ((BindingProvider) calculatorWS).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, "limited");
        ((BindingProvider) calculatorWS).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, "good");

        int result = calculatorWS.add(1, 1);
        Assertions.assertThat(result).isEqualTo(2);
    }

    @Test(expected = ServerSOAPFaultException.class)
    public void limited_valid_user_account_calling_denied_method() {
        CalculatorService calculatorService = new CalculatorService();
        CalculatorWS calculatorWS = calculatorService.getCalculatorSoapPort();
        ((BindingProvider) calculatorWS).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, "http://localhost:" + wsPort + "/ws/calculator");
        ((BindingProvider) calculatorWS).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, "limited");
        ((BindingProvider) calculatorWS).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, "good");

        calculatorWS.minus(1, 1);
        fail("should have failed since access is denied");
    }

    @Test
    public void full_valid_user_account_calling_all_methods() {
        CalculatorService calculatorService = new CalculatorService();
        CalculatorWS calculatorWS = calculatorService.getCalculatorSoapPort();
        ((BindingProvider) calculatorWS).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, "http://localhost:" + wsPort + "/ws/calculator");
        ((BindingProvider) calculatorWS).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, "full");
        ((BindingProvider) calculatorWS).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, "good");

        int result = calculatorWS.add(1, 1);
        Assertions.assertThat(result).isEqualTo(2);

        int result2 = calculatorWS.minus(1, 1);
        Assertions.assertThat(result2).isEqualTo(0);
    }

    @Test(expected = ClientTransportException.class)
    public void invalid_user_account() {
        CalculatorService calculatorService = new CalculatorService();
        CalculatorWS calculatorWS = calculatorService.getCalculatorSoapPort();
        ((BindingProvider) calculatorWS).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, "http://localhost:" + wsPort + "/ws/calculator");
        ((BindingProvider) calculatorWS).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, "full");
        ((BindingProvider) calculatorWS).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, "bad");

        calculatorWS.add(1, 1);
    }

    @Test(expected = ClientTransportException.class)
    public void should_launch_Exception() {
        CalculatorService calculatorService = new CalculatorService();
        CalculatorWS calculatorWS = calculatorService.getCalculatorSoapPort();
        ((BindingProvider) calculatorWS).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, "http://localhost:" + wsPort + "/ws/calculator");
        ((BindingProvider) calculatorWS).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, "full");
        ((BindingProvider) calculatorWS).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, "bad");

        calculatorWS.add(1, 1);
    }


    public void testhandlerNopassword() {
        CalculatorService calculatorService = new CalculatorService();
        CalculatorWS calculatorWS = calculatorService.getCalculatorSoapPort();
        ((BindingProvider) calculatorWS).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, "http://localhost:" + wsPort + "/ws/calculator");
        ((BindingProvider) calculatorWS).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, "full");
        ((BindingProvider) calculatorWS).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, "good");


    }
}
