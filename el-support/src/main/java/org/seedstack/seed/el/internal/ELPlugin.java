/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.el.internal;

import org.seedstack.seed.core.api.SeedException;
import org.seedstack.seed.el.spi.ELHandler;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.api.plugin.request.ClasspathScanRequest;
import io.nuun.kernel.core.AbstractPlugin;
import net.jodah.typetools.TypeResolver;
import org.kametic.specifications.Specification;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author pierre.thirouin@ext.mpsa.com
 *         Date: 27/06/2014
 */
public class ELPlugin extends AbstractPlugin {

    private final Specification<Class<?>> specificationELHandlers = classImplements(ELHandler.class);
    private Map<Class<? extends Annotation>, Class<ELHandler>> elMap = new HashMap<Class<? extends Annotation>, Class<ELHandler>>();

    @Override
    public String name() {
        return "seed-el-support";
    }

    @Override
    public Collection<ClasspathScanRequest> classpathScanRequests() {
        return classpathScanRequestBuilder().specification(specificationELHandlers).build();
    }

    @SuppressWarnings("unchecked")
    @Override
    public InitState init(InitContext initContext) {
        // Scan all the ExpressionLanguageHandler
        Map<Specification, Collection<Class<?>>> scannedTypesBySpecification = initContext.scannedTypesBySpecification();
        Collection<Class<?>> elHandlerClasses = scannedTypesBySpecification.get(specificationELHandlers);

        // Look for their type parameters
        for (Class<?> elHandlerClass : elHandlerClasses) {
            Class<Annotation> typeParameterClass = (Class<Annotation>) TypeResolver.resolveRawArguments(ELHandler.class, (Class<ELHandler>) elHandlerClass)[0];
            // transform this type parameters in a map of annotation, ExpressionHandler
            if (elMap.get(typeParameterClass) != null) {
                throw SeedException.createNew(ELErrorCode.EL_ANNOTATION_IS_ALREADY_BIND)
                        .put("annotation", typeParameterClass.getSimpleName())
                        .put("handler", elHandlerClass);
            }

            elMap.put(typeParameterClass, (Class<ELHandler>) elHandlerClass);
        }

        return InitState.INITIALIZED;
    }

    @Override
    public Object nativeUnitModule() {
        return new ELModule(elMap);
    }

}
