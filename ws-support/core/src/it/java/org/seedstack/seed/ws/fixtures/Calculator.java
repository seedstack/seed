/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.ws.fixtures;


import org.seedstack.seed.it.api.ITBind;
import org.seedstack.wsdl.seed.calculator.ImaginaryNumber;


@ITBind
public class Calculator {

    public int add(int one, int two){
        return one+two;
    }

    public ImaginaryNumber addImaginary(ImaginaryNumber one, ImaginaryNumber two){
        ImaginaryNumber imaginaryNumber = new ImaginaryNumber();
        imaginaryNumber.setReal(one.getReal() + two.getReal());
        imaginaryNumber.setImaginary(one.getImaginary() + two.getImaginary());
        return imaginaryNumber;
    }
    public int minus(int one, int two){
        return one-two;
    }
}
