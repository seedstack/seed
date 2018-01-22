/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.core.internal.el;

import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.api.plugin.request.ClasspathScanRequest;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.el.ELContext;
import net.jodah.typetools.TypeResolver;
import org.kametic.specifications.Specification;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.core.internal.AbstractSeedPlugin;
import org.seedstack.seed.el.spi.ELHandler;
import org.seedstack.shed.reflect.Classes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ELPlugin extends AbstractSeedPlugin {
    static final Optional<Class<ELContext>> EL3_OPTIONAL = Classes.optional("javax.el.StandardELContext");
    static final Optional<Class<ELContext>> JUEL_OPTIONAL = Classes.optional("de.odysseus.el.util.SimpleContext");
    private static final Logger LOGGER = LoggerFactory.getLogger(ELPlugin.class);
    private static final Optional<Class<Object>> EL_OPTIONAL = Classes.optional("javax.el.Expression");
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
    public InitState initialize(InitContext initContext) {
        if (EL_OPTIONAL.isPresent()) {
            Map<Class<? extends Annotation>, Class<ELHandler>> elMap = new HashMap<>();

            // Scan all the ExpressionLanguageHandler
            Map<Specification, Collection<Class<?>>> scannedTypesBySpecification = initContext
                    .scannedTypesBySpecification();
            Collection<Class<?>> elHandlerClasses = scannedTypesBySpecification.get(specificationELHandlers);

            // Look for their type parameters
            for (Class<?> elHandlerClass : elHandlerClasses) {
                Class<Annotation> typeParameterClass = (Class<Annotation>) TypeResolver.resolveRawArguments(
                        ELHandler.class, (Class<ELHandler>) elHandlerClass)[0];
                // transform this type parameters in a map of annotation, ExpressionHandler
                if (elMap.get(typeParameterClass) != null) {
                    throw SeedException.createNew(ExpressionLanguageErrorCode.EL_ANNOTATION_IS_ALREADY_BIND)
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

    public boolean isEnabled() {
        return EL_OPTIONAL.isPresent();
    }

    public boolean isLevel3() {
        return isEnabled() && EL3_OPTIONAL.isPresent();
    }

    public boolean isFunctionMappingAvailable() {
        return isEnabled() && (EL3_OPTIONAL.isPresent() || JUEL_OPTIONAL.isPresent());
    }
}
