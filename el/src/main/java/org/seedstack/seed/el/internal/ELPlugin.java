/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.el.internal;

import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.api.plugin.request.ClasspathScanRequest;
import io.nuun.kernel.core.AbstractPlugin;
import net.jodah.typetools.TypeResolver;
import org.kametic.specifications.Specification;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.core.utils.SeedReflectionUtils;
import org.seedstack.seed.el.spi.ELHandler;
import org.seedstack.seed.spi.dependency.Maybe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.el.ELContext;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author pierre.thirouin@ext.mpsa.com
 * @author adrien.lauer@mpsa.com
 */
public class ELPlugin extends AbstractPlugin {
    private static final Logger LOGGER = LoggerFactory.getLogger(ELPlugin.class);
    private static final Maybe<Class<Object>> EL_MAYBE = SeedReflectionUtils.forName("javax.el.Expression");
    static final Maybe<Class<ELContext>> EL3_MAYBE = SeedReflectionUtils.forName("javax.el.StandardELContext");
    static final Maybe<Class<ELContext>> JUEL_MAYBE = SeedReflectionUtils.forName("de.odysseus.el.util.SimpleContext");

    private final Specification<Class<?>> specificationELHandlers = classImplements(ELHandler.class);
    private ELModule elModule;

    @Override
    public String name() {
        return "el";
    }

    @Override
    public Collection<ClasspathScanRequest> classpathScanRequests() {
        return classpathScanRequestBuilder().specification(specificationELHandlers).build();
    }

    @SuppressWarnings("unchecked")
    @Override
    public InitState init(InitContext initContext) {
        if (EL_MAYBE.isPresent()) {
            Map<Class<? extends Annotation>, Class<ELHandler>> elMap = new HashMap<Class<? extends Annotation>, Class<ELHandler>>();

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

            elModule = new ELModule(elMap);
        } else {
            LOGGER.debug("Java EL is not present in the classpath, EL support disabled");
        }

        return InitState.INITIALIZED;
    }

    @Override
    public Object nativeUnitModule() {
        return elModule;
    }

    public boolean isDisabled() {
        return elModule == null;
    }
}
