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
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.el.ELContext;
import javax.el.ExpressionFactory;
import net.jodah.typetools.TypeResolver;
import org.kametic.specifications.Specification;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.core.internal.AbstractSeedPlugin;
import org.seedstack.seed.el.spi.ELHandler;
import org.seedstack.shed.reflect.Classes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ELPlugin extends AbstractSeedPlugin {
    static final Class<? extends ELContext> EL_3_CONTEXT_CLASS;
    static final Class<? extends ELContext> JUEL_CONTEXT_CLASS;
    private static final Object EXPRESSION_FACTORY;
    private static final Logger LOGGER = LoggerFactory.getLogger(ELPlugin.class);
    private final Specification<Class<?>> specificationELHandlers = classImplements(ELHandler.class);
    private ELModule elModule;

    static {
        if (Classes.optional("javax.el.Expression").isPresent()) {
            EXPRESSION_FACTORY = buildExpressionFactory();
            EL_3_CONTEXT_CLASS = Classes.optional("javax.el.StandardELContext")
                    .<Class<? extends ELContext>>map(objectClass -> objectClass.asSubclass(ELContext.class))
                    .orElse(null);
            JUEL_CONTEXT_CLASS = Classes.optional("de.odysseus.el.util.SimpleContext")
                    .<Class<? extends ELContext>>map(objectClass -> objectClass.asSubclass(ELContext.class))
                    .orElse(null);
        } else {
            EXPRESSION_FACTORY = null;
            EL_3_CONTEXT_CLASS = null;
            JUEL_CONTEXT_CLASS = null;
        }
    }

    public static boolean isEnabled() {
        return EXPRESSION_FACTORY != null;
    }

    public static boolean isLevel3() {
        return isEnabled() && EL_3_CONTEXT_CLASS != null;
    }

    public static boolean isFunctionMappingAvailable() {
        return isEnabled() && (EL_3_CONTEXT_CLASS != null || JUEL_CONTEXT_CLASS != null);
    }

    public static Object getExpressionFactory() {
        if (!isEnabled()) {
            throw new IllegalStateException("EL expression factory is not available");
        }
        return EXPRESSION_FACTORY;
    }

    private static Object buildExpressionFactory() {
        try {
            // EL will use the TCCL to find the implementation
            return ExpressionFactory.newInstance();
        } catch (Throwable t1) {
            // If TCCL failed, we use the ClassLoader that loaded the ELPlugin
            final ClassLoader originalTCCL = run(Thread.currentThread()::getContextClassLoader);
            try {
                run((PrivilegedAction<Void>) () -> {
                    Thread.currentThread().setContextClassLoader(ELPlugin.class.getClassLoader());
                    return null;
                });
                return ExpressionFactory.newInstance();
            } catch (Throwable t2) {
                throw SeedException.wrap(t2, ELErrorCode.UNABLE_TO_INSTANTIATE_EXPRESSION_FACTORY);
            } finally {
                // restore original TCCL
                run((PrivilegedAction<Void>) () -> {
                    Thread.currentThread().setContextClassLoader(originalTCCL);
                    return null;
                });
            }
        }
    }

    private static <T> T run(PrivilegedAction<T> action) {
        return System.getSecurityManager() != null ? AccessController.doPrivileged(action) : action.run();
    }

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
        if (isEnabled()) {
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

            elModule = new ELModule((ExpressionFactory) EXPRESSION_FACTORY, elMap);
        } else {
            LOGGER.debug("Java EL is not present in the classpath, EL support disabled");
        }

        return InitState.INITIALIZED;
    }

    @Override
    public Object nativeUnitModule() {
        return elModule;
    }
}
