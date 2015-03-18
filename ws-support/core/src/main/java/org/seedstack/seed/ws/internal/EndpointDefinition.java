/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.ws.internal;

import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.server.SDDocumentSource;
import org.apache.commons.configuration.Configuration;
import org.xml.sax.EntityResolver;

import javax.xml.namespace.QName;

/**
 * This class holds all information to create a Web Service endpoint.
 *
 * @author adrien.lauer@mpsa.com
 */
public class EndpointDefinition {
    private final java.lang.Class<?> implementorClass;
    private final boolean processHandlerAnnotation;
    private final javax.xml.namespace.QName serviceName;
    private final javax.xml.namespace.QName portName;
    private final com.sun.xml.ws.api.WSBinding binding;
    private final com.sun.xml.ws.api.server.SDDocumentSource primaryWsdl;
    private final org.xml.sax.EntityResolver resolver;
    private final boolean isTransportSynchronous;
    private final String url;
    private final Configuration configuration;

    /**
     * Create the endpoint definition.
     * @param implementorClass the WS implementation class.
     * @param processHandlerAnnotation if handler annotations should be processed.
     * @param serviceName the service name.
     * @param portName the port name.
     * @param binding the binding.
     * @param primaryWsdl the WSDL.
     * @param resolver the entity resolver.
     * @param isTransportSynchronous if transport is synchronous.
     * @param url the endpoint url.
     * @param configuration the endpoint configuration.
     */
    public EndpointDefinition(Class<?> implementorClass, boolean processHandlerAnnotation, QName serviceName, QName portName, WSBinding binding, SDDocumentSource primaryWsdl, EntityResolver resolver, boolean isTransportSynchronous, String url, Configuration configuration) { // NOSONAR
        this.implementorClass = implementorClass;
        this.processHandlerAnnotation = processHandlerAnnotation;
        this.serviceName = serviceName;
        this.portName = portName;
        this.binding = binding;
        this.primaryWsdl = primaryWsdl;
        this.resolver = resolver;
        this.isTransportSynchronous = isTransportSynchronous;
        this.url = url;
        this.configuration = configuration;
    }

    /**
     * @return the WS implementation class.
     */
    public Class<?> getImplementorClass() {
        return implementorClass;
    }

    /**
     * @return true if handler annotations should be processed, false otherwise.
     */
    public boolean isProcessHandlerAnnotation() {
        return processHandlerAnnotation;
    }

    /**
     * @return the service name.
     */
    public QName getServiceName() {
        return serviceName;
    }

    /**
     * @return the port name.
     */
    public QName getPortName() {
        return portName;
    }

    /**
     * @return the binding.
     */
    public WSBinding getBinding() {
        return binding;
    }

    /**
     * @return the WSDL.
     */
    public SDDocumentSource getPrimaryWsdl() {
        return primaryWsdl;
    }

    /**
     * @return the entity resolver.
     */
    public EntityResolver getResolver() {
        return resolver;
    }

    /**
     * @return true if the transport is synchronous, false otherwise.
     */
    public boolean isTransportSynchronous() {
        return isTransportSynchronous;
    }

    /**
     * @return the endpoint url.
     */
    public String getUrl() {
        return url;
    }

    /**
     * @return the endpoint configuration subset.
     */
    public Configuration getConfiguration() {
        return configuration;
    }
}
