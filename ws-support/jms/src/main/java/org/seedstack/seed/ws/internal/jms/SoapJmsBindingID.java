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
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.pipe.Codec;
import com.sun.xml.ws.binding.WebServiceFeatureList;
import com.sun.xml.ws.encoding.SOAPBindingCodec;

import javax.xml.ws.soap.MTOMFeature;
import java.util.HashMap;
import java.util.Map;

final class SoapJmsBindingID {
    /**
     * Constant that represents SOAP1.2/JMS.
     */
    static final SOAPJMSImpl SOAP12_JMS = new SOAPJMSImpl(
            SOAPVersion.SOAP_12, SoapJmsBinding.SOAPJMS_BINDING, true);
    /**
     * Constant that represents SOAP1.2/JMS with MTOM.
     */
    static final SOAPJMSImpl SOAP12_JMS_MTOM = new SOAPJMSImpl(
            SOAPVersion.SOAP_12, SoapJmsBinding.SOAPJMS_BINDING, true, true);
    /**
     * Constant that represents SOAP1.1/JMS.
     */
    static final SOAPJMSImpl SOAP11_JMS = new SOAPJMSImpl(
            SOAPVersion.SOAP_11, SoapJmsBinding.SOAPJMS_BINDING, true);
    /**
     * Constant that represents SOAP1.1/JMS with MTOM.
     */
    static final SOAPJMSImpl SOAP11_JMS_MTOM = new SOAPJMSImpl(
            SOAPVersion.SOAP_11, SoapJmsBinding.SOAPJMS_BINDING, true, true);

    private SoapJmsBindingID() {
    }

    private abstract static class Impl extends BindingID {
        final SOAPVersion version;
        private final String lexical;
        private final boolean canGenerateWSDL;

        private Impl(SOAPVersion version, String lexical, boolean canGenerateWSDL) {
            this.version = version;
            this.lexical = lexical;
            this.canGenerateWSDL = canGenerateWSDL;
        }

        @Override
        public SOAPVersion getSOAPVersion() {
            return version;
        }

        @Override
        public String toString() {
            return lexical;
        }

        @Override
        public boolean canGenerateWSDL() {
            return canGenerateWSDL;
        }
    }

    private static final class SOAPJMSImpl extends Impl implements Cloneable {
        static final String MTOM_PARAM = "mtom";
        private final Map<String, String> parameters = new HashMap<String, String>();

        private SOAPJMSImpl(SOAPVersion version, String lexical, boolean canGenerateWSDL) {
            super(version, lexical, canGenerateWSDL);
        }

        private SOAPJMSImpl(SOAPVersion version, String lexical, boolean canGenerateWSDL, boolean mtomEnabled) {
            this(version, lexical, canGenerateWSDL);
            if (mtomEnabled) {
                parameters.put(MTOM_PARAM, "true");
            } else {
                parameters.put(MTOM_PARAM, "false");
            }
        }

        @Override
        public Codec createEncoder(WSBinding binding) {
            return new SOAPBindingCodec(binding.getFeatures());
        }

        @Override
        public WebServiceFeatureList createBuiltinFeatureList() {
            WebServiceFeatureList r = super.createBuiltinFeatureList();
            Boolean mtom = isMTOMEnabled();
            if (mtom != null) {
                r.add(new MTOMFeature(mtom));
            }
            return r;
        }

        @Override
        public String getParameter(String parameterName, String defaultValue) {
            if (parameters.get(parameterName) == null) {
                return super.getParameter(parameterName, defaultValue);
            }
            return parameters.get(parameterName);
        }

        @Override
        public SOAPJMSImpl clone() throws CloneNotSupportedException {
            return (SOAPJMSImpl) super.clone();
        }

        private Boolean isMTOMEnabled() {
            String mtom = parameters.get(MTOM_PARAM);
            if (mtom == null) {
                return null;
            } else {
                return Boolean.valueOf(mtom);
            }
        }
    }
}
