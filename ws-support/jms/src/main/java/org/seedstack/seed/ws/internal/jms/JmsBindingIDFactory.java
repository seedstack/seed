/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.ws.internal.jms;

import com.sun.xml.ws.api.BindingID;
import com.sun.xml.ws.api.BindingIDFactory;
import com.sun.xml.ws.api.SOAPVersion;

import javax.xml.ws.WebServiceException;

/**
 * Binding ID factory for JMS transport.
 *
 * @author adrien.lauer@mpsa.com
 */
public class JmsBindingIDFactory extends BindingIDFactory {
    @Override
    public BindingID parse(String lexical) throws WebServiceException {
        if (SoapJmsBinding.SOAPJMS_BINDING.equals(lexical)) {
            return SoapJmsBindingID.SOAP11_JMS;
        } else {
            return null;
        }
    }

    @Override
    public BindingID create(String transport, SOAPVersion soapVersion) throws WebServiceException {
        if (SoapJmsBinding.SOAPJMS_BINDING.equals(transport)) {
            if (soapVersion.equals(SOAPVersion.SOAP_11)) {
                return SoapJmsBindingID.SOAP11_JMS;
            } else if (soapVersion.equals(SOAPVersion.SOAP_12)) {
                return BindingID.SOAP12_HTTP;
            }

            return null;
        }

        return null;
    }
}
