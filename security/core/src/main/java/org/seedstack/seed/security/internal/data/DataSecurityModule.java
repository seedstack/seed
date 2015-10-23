/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.internal.data;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.matcher.Matcher;
import com.google.inject.matcher.Matchers;
import com.google.inject.multibindings.MapBinder;
import net.jodah.typetools.TypeResolver;
import org.seedstack.seed.security.api.data.DataSecurityService;
import org.seedstack.seed.security.api.data.Secured;
import org.seedstack.seed.security.spi.data.DataSecurityHandler;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Set;

import static org.seedstack.seed.core.utils.SeedReflectionUtils.allParametersAnnotationsFromAncestors;
import static org.seedstack.seed.core.utils.SeedReflectionUtils.isPresent;
import static org.seedstack.seed.core.utils.SeedReflectionUtils.methodsFromAncestors;

/**
 * Configuration Unit regarding Data Security
 *
 * @author epo.jemba@ext.mpsa.com
 * @author pierre.thirouin@ext.mpsa.com
 */
public class DataSecurityModule extends AbstractModule {
    private static final TypeLiteral<DataSecurityHandler<?>> MAP_TYPE_LITERAL = new TypeLiteral<DataSecurityHandler<?>>() {};
    private Collection<Class<? extends DataSecurityHandler<?>>> dataSecurityHandlers;

    public DataSecurityModule(Collection<Class<? extends DataSecurityHandler<?>>> dataSecurityHandlers) {
        this.dataSecurityHandlers = dataSecurityHandlers;
    }

    @Override
    protected void configure() {
        bind(DataSecurityService.class).to(DataSecurityServiceInternal.class);

        MapBinder<Object, DataSecurityHandler<?>> mapBinder = MapBinder.newMapBinder(binder(), TypeLiteral.get(Object.class), MAP_TYPE_LITERAL);
        for (Class<? extends DataSecurityHandler<?>> dSecClass : dataSecurityHandlers) {
            @SuppressWarnings("unchecked")
            Object typeParameterClass = TypeResolver.resolveRawArguments(DataSecurityHandler.class, (Class<DataSecurityHandler<?>>) dSecClass)[0];
            mapBinder.addBinding(typeParameterClass).to(dSecClass);

            // TODO : pour l'augmentation des features du DataSecurityHandler
            // 1 ) Créer une interface SecuredObjectProvider qui fournira la ConventionSpecification application soit (Field.class , Method.class , Constructor.class)
            //     elle doit avoir une methode get en plus de apply/specify/whatv
            //     un simple get
            // 2) Créer la Clé composite en question
        }

        // @Secured interceptor
        DataSecurityInterceptor dataSecurityInterceptor = new DataSecurityInterceptor();
        requestInjection(dataSecurityInterceptor);
        bindInterceptor(Matchers.any(), securedMethods(), dataSecurityInterceptor);
    }

    private Matcher<? super Method> securedMethods() {
        return new AbstractMatcher<Method>() {

            @Override
            public boolean matches(Method candidate) {
                if (candidate.isSynthetic()) {
                    return false;
                }

                boolean toBeSecured = isPresent(methodsFromAncestors(candidate), Secured.class);

                if (!toBeSecured) {
                    Set<Annotation[][]> methodsParametersAnnotationsFromAncestors = allParametersAnnotationsFromAncestors(candidate);

                    outer:
                    for (Annotation[][] paramAnnos : methodsParametersAnnotationsFromAncestors) {
                        if (paramAnnos != null) {
                            for (Annotation[] annos : paramAnnos) {
                                if (annos != null) {
                                    for (Annotation anno : annos) {
                                        if (anno.annotationType().equals(Secured.class)) {
                                            toBeSecured = true;
                                            break outer;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                return toBeSecured;
            }
        };
    }
}
