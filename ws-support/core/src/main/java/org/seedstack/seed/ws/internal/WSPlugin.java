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

import com.google.common.collect.ImmutableList;
import com.oracle.webservices.api.databinding.DatabindingModeFeature;
import com.oracle.webservices.api.databinding.ExternalMetadataFeature;
import com.sun.xml.ws.api.BindingID;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.databinding.MetadataReader;
import com.sun.xml.ws.api.server.Container;
import com.sun.xml.ws.api.server.SDDocumentSource;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.binding.WebServiceFeatureList;
import com.sun.xml.ws.resources.ServerMessages;
import com.sun.xml.ws.server.EndpointFactory;
import com.sun.xml.ws.server.ServerRtException;
import com.sun.xml.ws.transport.http.ResourceLoader;
import com.sun.xml.ws.transport.http.server.HttpEndpoint;
import com.sun.xml.ws.transport.http.server.ServerAdapterList;
import com.sun.xml.ws.util.xml.XmlUtil;
import com.sun.xml.wss.RealmAuthenticationAdapter;
import io.nuun.kernel.api.Plugin;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.Context;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.api.plugin.request.BindingRequest;
import io.nuun.kernel.api.plugin.request.ClasspathScanRequest;
import io.nuun.kernel.core.AbstractPlugin;
import org.apache.commons.configuration.Configuration;
import org.kametic.specifications.Specification;
import org.seedstack.seed.core.api.SeedException;
import org.seedstack.seed.core.internal.application.ApplicationPlugin;
import org.seedstack.seed.core.utils.SeedReflectionUtils;
import org.seedstack.seed.core.utils.SeedSpecifications;
import org.seedstack.seed.ws.adapters.NoSecurityRealmAuthenticationAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.EntityResolver;

import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.handler.LogicalHandler;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.http.HTTPBinding;
import javax.xml.ws.soap.MTOMFeature;
import javax.xml.ws.soap.SOAPBinding;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * This plugin provides standalone JAX-WS integration.
 *
 * @author emmanuel.vinel@mpsa.com
 * @author adrien.lauer@mpsa.com
 */
public class WSPlugin extends AbstractPlugin {
    public static final String WS_PLUGIN_CONFIGURATION_PREFIX = "org.seedstack.seed.ws";
    public static final List<String> SUPPORTED_BINDINGS = ImmutableList.of(SOAPBinding.SOAP11HTTP_BINDING, SOAPBinding.SOAP12HTTP_BINDING, SOAPBinding.SOAP11HTTP_MTOM_BINDING, SOAPBinding.SOAP12HTTP_MTOM_BINDING);

    private static final Logger LOGGER = LoggerFactory.getLogger(WSPlugin.class);
    private static final String XSD_REGEX = ".*\\.xsd";
    private static final String WSDL_REGEX = ".*\\.wsdl";
    private static final String ENDPOINT_NAME_ATTRIBUTE = "endpointName";
    private static final String IMPLEMENTATION_CLASS_ATTRIBUTE = "implementationClass";
    private static final String WSDL_LOCATION_ATTRIBUTE = "wsdlLocation";

    private final Specification<Class<?>> webServiceAnnotatedCandidateClass = and(classAnnotatedWith(WebService.class), not(SeedSpecifications.classIsInterface()), not(SeedSpecifications.classIsAbstract()));
    private final Specification<Class<?>> webServiceClientAnnotatedCandidateClass = and(classAnnotatedWith(WebServiceClient.class), not(SeedSpecifications.classIsInterface()), not(SeedSpecifications.classIsAbstract()));
    private final Specification<Class<?>> handler = and(classImplements(SOAPHandler.class), or(classImplements(LogicalHandler.class)), or(classImplements(SOAPHandler.class)));

    private final ClassLoader classLoader = SeedReflectionUtils.findMostCompleteClassLoader(WSPlugin.class);
    private final Set<Class<?>> webServiceClasses = new HashSet<Class<?>>();
    private final Set<Class<?>> webServiceClientClasses = new HashSet<Class<?>>();

    private final Map<String, SDDocumentSource> docs = new HashMap<String, SDDocumentSource>();
    private final Map<String, EndpointDefinition> endpointDefinitions = new HashMap<String, EndpointDefinition>();
    private final Map<String, HttpEndpoint> httpEndpoints = new HashMap<String, HttpEndpoint>();
    private final ServerAdapterList serverAdapters = new ServerAdapterList();

    private ResourceLoader resourceLoader;
    private Configuration wsConfiguration;
    private Class<? extends RealmAuthenticationAdapter> realmAuthenticationAdapterClass;
    private boolean disableEndpointPublishing;

    @Override
    public String name() {
        return "seed-ws-plugin";
    }

    @Override
    public String pluginPackageRoot() {
        return "META-INF.ws";
    }

    @Override
    @SuppressWarnings("unchecked")
    public InitState init(InitContext initContext) {
        for (Plugin plugin : initContext.pluginsRequired()) {
            if (plugin instanceof ApplicationPlugin) {
                wsConfiguration = ((ApplicationPlugin) plugin).getApplication().getConfiguration().subset(WSPlugin.WS_PLUGIN_CONFIGURATION_PREFIX);
            }
        }

        if (wsConfiguration == null) {
            throw SeedException.createNew(WSErrorCode.NO_WS_CONFIGURATION);
        }

        for (String xsdResource : initContext.mapResourcesByRegex().get(XSD_REGEX)) {
            URL xsdResourceUrl = classLoader.getResource(xsdResource);
            if (xsdResourceUrl != null) {
                docs.put(xsdResourceUrl.toString(), SDDocumentSource.create(xsdResourceUrl));
            }
        }

        for (String wsdlResource : initContext.mapResourcesByRegex().get(WSDL_REGEX)) {
            URL wsdlResourceUrl = classLoader.getResource(wsdlResource);
            if (wsdlResourceUrl != null) {
                docs.put(wsdlResourceUrl.toString(), SDDocumentSource.create(wsdlResourceUrl));
            }
        }

        resourceLoader = new SeedResourceLoader(classLoader, docs.keySet());

        Map<Specification, Collection<Class<?>>> scannedTypesBySpecification = initContext.scannedTypesBySpecification();

        Collection<Class<?>> webServiceClientAnnotatedClassCandidates = scannedTypesBySpecification.get(webServiceClientAnnotatedCandidateClass);
        for (Class<?> webServiceClientAnnotatedClassCandidate : webServiceClientAnnotatedClassCandidates) {
            webServiceClientClasses.add(webServiceClientAnnotatedClassCandidate);
        }

        Collection<Class<?>> webServiceAnnotatedClassCandidates = scannedTypesBySpecification.get(webServiceAnnotatedCandidateClass);
        for (Class webServiceAnnotatedClassCandidate : webServiceAnnotatedClassCandidates) {
            webServiceClasses.add(webServiceAnnotatedClassCandidate);
        }

        String[] endpointNames = wsConfiguration.getStringArray("endpoints");
        if (endpointNames.length == 0) {
            LOGGER.info("No WS endpoint declared");
        }

        for (String endpointName : endpointNames) {
            endpointDefinitions.put(endpointName, createEndpointDefinition(endpointName, wsConfiguration.subset("endpoint." + endpointName)));
        }

        String realmAuthenticationAdapterClassname = wsConfiguration.getString("wss.realm-authentication-adapter", NoSecurityRealmAuthenticationAdapter.class.getCanonicalName());
        Class<?> loadedClass;

        try {
            loadedClass = Class.forName(realmAuthenticationAdapterClassname);
        } catch (ClassNotFoundException e) {
            throw SeedException.wrap(e, WSErrorCode.UNABLE_TO_LOAD_REALM_AUTHENTICATION_ADAPTER_CLASS).put("classname", realmAuthenticationAdapterClassname);
        }

        if (RealmAuthenticationAdapter.class.isAssignableFrom(loadedClass)) {
            realmAuthenticationAdapterClass = (Class<? extends RealmAuthenticationAdapter>) loadedClass;
        } else {
            throw SeedException.createNew(WSErrorCode.INVALID_REALM_AUTHENTICATION_ADAPTER_CLASS);
        }

        return InitState.INITIALIZED;
    }

    @Override
    public void start(Context context) {
        for (Map.Entry<String, EndpointDefinition> wsEndpointEntry : getEndpointDefinitions(SUPPORTED_BINDINGS).entrySet()) {
            String endpointName = wsEndpointEntry.getKey();

            if (!disableEndpointPublishing) {
                String urlString = wsEndpointEntry.getValue().getUrl();
                if (urlString == null || urlString.isEmpty()) {
                    throw SeedException.createNew(WSErrorCode.ENDPOINT_URL_MISSING).put(ENDPOINT_NAME_ATTRIBUTE, endpointName);
                }

                URL url;
                try {
                    url = new URL(urlString);
                } catch (MalformedURLException e) {
                    throw SeedException.wrap(e, WSErrorCode.MALFORMED_ENDPOINT_URL).put(ENDPOINT_NAME_ATTRIBUTE, endpointName);
                }

                LOGGER.info("Publishing WS endpoint {} on {}", endpointName, url);
                HttpEndpoint httpEndpoint = new HttpEndpoint(null, serverAdapters.createAdapter(endpointName, url.getPath(), createWSEndpoint(wsEndpointEntry.getValue(), null)));
                httpEndpoints.put(endpointName, httpEndpoint);
                httpEndpoint.publish(url.toExternalForm());
            }
        }
    }

    @Override
    public void stop() {
        for (Map.Entry<String, HttpEndpoint> httpEndpointEntry : httpEndpoints.entrySet()) {
            LOGGER.info("Stopping WS endpoint {}", httpEndpointEntry.getKey());
            httpEndpointEntry.getValue().stop();
        }
    }

    @Override
    public Collection<Class<? extends Plugin>> requiredPlugins() {
        Collection<Class<? extends Plugin>> plugins = new ArrayList<Class<? extends Plugin>>();
        plugins.add(ApplicationPlugin.class);
        return plugins;
    }

    @Override
    public Collection<ClasspathScanRequest> classpathScanRequests() {
        return classpathScanRequestBuilder()
                .specification(webServiceAnnotatedCandidateClass)
                .specification(webServiceClientAnnotatedCandidateClass)
                .resourcesRegex(XSD_REGEX)
                .resourcesRegex(WSDL_REGEX)
                .build();
    }

    @Override
    public Collection<BindingRequest> bindingRequests() {
        return bindingRequestsBuilder().specification(handler).build();
    }

    @Override
    public Object nativeUnitModule() {
        return new WSModule(webServiceClasses, webServiceClientClasses, realmAuthenticationAdapterClass);

    }

    /**
     * Return the endpoint definitions corresponding to the list of supplied supported bindings.
     *
     * @param supportedBindings the supported bindings to filter the list of endpoint definitions.
     * @return the map of names to endpoint definitions.
     */
    public Map<String, EndpointDefinition> getEndpointDefinitions(List<String> supportedBindings) {
        Map<String, EndpointDefinition> wsEndpoints = new HashMap<String, EndpointDefinition>();
        for (Map.Entry<String, EndpointDefinition> endpointDefinitionEntry : this.endpointDefinitions.entrySet()) {
            if (supportedBindings.contains(endpointDefinitionEntry.getValue().getBinding().getBindingId().toString())) {
                wsEndpoints.put(endpointDefinitionEntry.getKey(), endpointDefinitionEntry.getValue());
            }
        }

        return wsEndpoints;
    }

    /**
     * Create a WSEndpoint object base on the supplied EndpointDefinition and Container.
     *
     * @param endpointDefinition the endpoint definition the returned WSEndpoint will be based on.
     * @param container          the runtime container.
     * @return the WSEndpoint instance.
     */
    @SuppressWarnings("unchecked")
    public WSEndpoint<?> createWSEndpoint(EndpointDefinition endpointDefinition, Container container) {
        return WSEndpoint.create(
                endpointDefinition.getImplementorClass(),
                endpointDefinition.isProcessHandlerAnnotation(),
                new SeedInstanceResolver((Class<Object>) endpointDefinition.getImplementorClass()).createInvoker(),
                endpointDefinition.getServiceName(),
                endpointDefinition.getPortName(),
                container,
                endpointDefinition.getBinding(),
                endpointDefinition.getPrimaryWsdl(),
                docs.values(),
                endpointDefinition.getResolver(),
                endpointDefinition.isTransportSynchronous()
        );
    }

    private EndpointDefinition createEndpointDefinition(String endpoint, Configuration endpointConfiguration) {
        // Implementation class
        String implementation = endpointConfiguration.getString("implementation");
        if (implementation == null || implementation.isEmpty()) {
            throw SeedException.createNew(WSErrorCode.IMPLEMENTATION_CLASS_MISSING).put(ENDPOINT_NAME_ATTRIBUTE, endpoint);
        }

        Class<?> implementationClass;
        try {
            implementationClass = Class.forName(implementation);
        } catch (ClassNotFoundException e) {
            throw SeedException.wrap(e, WSErrorCode.UNABLE_TO_LOAD_IMPLEMENTATION_CLASS).put(ENDPOINT_NAME_ATTRIBUTE, endpoint).put(IMPLEMENTATION_CLASS_ATTRIBUTE, implementation);
        }

        // External metadata
        MetadataReader metadataReader = null;
        String externalMetadata = endpointConfiguration.getString("external-metadata");
        if (externalMetadata != null) {
            metadataReader = ExternalMetadataFeature.builder().addResources(externalMetadata).build().getMetadataReader(SeedReflectionUtils.findMostCompleteClassLoader(implementationClass), false);
        }

        EndpointFactory.verifyImplementorClass(implementationClass, metadataReader);

        // Service name
        String serviceName = endpointConfiguration.getString("service-name");
        QName serviceQName;
        if (serviceName == null || serviceName.isEmpty()) {
            WebService annotation = implementationClass.getAnnotation(WebService.class);
            if (annotation != null && !annotation.targetNamespace().isEmpty() && !annotation.serviceName().isEmpty()) {
                serviceQName = new QName(annotation.targetNamespace(), annotation.serviceName());
            } else {
                serviceQName = EndpointFactory.getDefaultServiceName(implementationClass, metadataReader);
            }
        } else {
            serviceQName = QName.valueOf(serviceName);
        }

        // Service port
        String portName = endpointConfiguration.getString("port-name");
        QName portQName;
        if (portName == null || portName.isEmpty()) {
            WebService annotation = implementationClass.getAnnotation(WebService.class);
            if (annotation != null && !annotation.targetNamespace().isEmpty() && !annotation.portName().isEmpty()) {
                portQName = new QName(annotation.targetNamespace(), annotation.portName());
            } else {
                portQName = EndpointFactory.getDefaultPortName(serviceQName, implementationClass, metadataReader);
            }
        } else {
            portQName = QName.valueOf(portName);
        }

        // Binding
        String binding = endpointConfiguration.getString("binding");
        if (binding != null) {
            binding = getBindingIdForToken(binding);
        }

        WSBinding wsBinding = createBinding(
                binding,
                implementationClass,
                endpointConfiguration.getBoolean("enable-mtom", null),
                endpointConfiguration.getInteger("mtom-treshold", null),
                endpointConfiguration.getString("data-binding-mode", null)
        );

        // WSDL
        String wsdlPath = endpointConfiguration.getString("wsdl", EndpointFactory.getWsdlLocation(implementationClass, metadataReader));
        if (wsdlPath == null || wsdlPath.isEmpty()) {
            throw SeedException.createNew(WSErrorCode.WSDL_LOCATION_MISSING).put(ENDPOINT_NAME_ATTRIBUTE, endpoint).put(IMPLEMENTATION_CLASS_ATTRIBUTE, implementation);
        }

        URL wsdlURL;
        try {
            wsdlURL = resourceLoader.getResource(wsdlPath);
        } catch (MalformedURLException e) {
            throw SeedException.wrap(e, WSErrorCode.UNABLE_TO_FIND_WSDL).put(ENDPOINT_NAME_ATTRIBUTE, endpoint).put(WSDL_LOCATION_ATTRIBUTE, wsdlPath);
        }

        if (wsdlURL == null) {
            throw SeedException.createNew(WSErrorCode.UNABLE_TO_FIND_WSDL).put(ENDPOINT_NAME_ATTRIBUTE, endpoint).put(WSDL_LOCATION_ATTRIBUTE, wsdlPath);
        }

        SDDocumentSource primaryWSDL = docs.get(wsdlURL.toExternalForm());
        assert primaryWSDL != null;

        // Entity resolver
        EntityResolver entityResolver;
        try {
            entityResolver = XmlUtil.createEntityResolver(resourceLoader.getCatalogFile());
        } catch (MalformedURLException e) {
            LOGGER.warn("Unable to create entity resolver", e);
            entityResolver = XmlUtil.createEntityResolver(null);
        }

        return new EndpointDefinition(implementationClass, true, serviceQName, portQName, wsBinding, primaryWSDL, entityResolver, false, endpointConfiguration.getString("url"), endpointConfiguration);
    }

    private WSBinding createBinding(String ddBindingId, Class implClass, Boolean mtomEnabled, Integer mtomThreshold, String dataBindingMode) {
        WebServiceFeatureList features;

        MTOMFeature mtomfeature = null;
        if (mtomEnabled != null) {
            if (mtomThreshold != null) {
                mtomfeature = new MTOMFeature(mtomEnabled, mtomThreshold);
            } else {
                mtomfeature = new MTOMFeature(mtomEnabled);
            }
        }

        BindingID bindingID;
        if (ddBindingId != null) {
            bindingID = BindingID.parse(ddBindingId);
            features = bindingID.createBuiltinFeatureList();

            if (checkMtomConflict(features.get(MTOMFeature.class), mtomfeature)) {
                throw new ServerRtException(ServerMessages.DD_MTOM_CONFLICT(ddBindingId, mtomEnabled));
            }
        } else {
            bindingID = BindingID.parse(implClass);

            features = new WebServiceFeatureList();
            if (mtomfeature != null) {
                features.add(mtomfeature);
            }
            features.addAll(bindingID.createBuiltinFeatureList());
        }

        if (dataBindingMode != null) {
            features.add(new DatabindingModeFeature(dataBindingMode));
        }

        return bindingID.createBinding(features.toArray());
    }

    private boolean checkMtomConflict(MTOMFeature lhs, MTOMFeature rhs) {
        return !(lhs == null || rhs == null) && lhs.isEnabled() ^ rhs.isEnabled();
    }

    private String getBindingIdForToken(String lexical) { // NOSONAR
        if ("##SOAP11_HTTP".equals(lexical)) {
            return SOAPBinding.SOAP11HTTP_BINDING;
        } else if ("##SOAP11_HTTP_MTOM".equals(lexical)) {
            return SOAPBinding.SOAP11HTTP_MTOM_BINDING;
        } else if ("##SOAP12_HTTP".equals(lexical)) {
            return SOAPBinding.SOAP12HTTP_BINDING;
        } else if ("##SOAP12_HTTP_MTOM".equals(lexical)) {
            return SOAPBinding.SOAP12HTTP_MTOM_BINDING;
        } else if ("##XML_HTTP".equals(lexical)) {
            return HTTPBinding.HTTP_BINDING;
        }
        return lexical;
    }

    /**
     * This method disables standalone endpoint publishing (used by WS web plugin which handles publication itself).
     */
    public void disableEndpointPublishing() {
        disableEndpointPublishing = true;
    }
}