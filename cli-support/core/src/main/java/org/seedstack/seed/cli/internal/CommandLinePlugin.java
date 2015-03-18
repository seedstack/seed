/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.cli.internal;

import org.seedstack.seed.cli.spi.CommandLineHandler;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.api.plugin.request.ClasspathScanRequest;
import io.nuun.kernel.core.AbstractPlugin;
import org.apache.commons.lang.ArrayUtils;
import org.kametic.specifications.AbstractSpecification;
import org.kametic.specifications.Specification;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This plugin enables to run {@link org.seedstack.seed.cli.spi.CommandLineHandler} through
 * {@link org.seedstack.seed.cli.SeedRunner}.
 *
 * @author epo.jemba@ext.mpsa.com
 */
public class CommandLinePlugin extends AbstractPlugin {
    private final Specification<Class<?>> cliHandlerSpec = and(ancestorImplements(CommandLineHandler.class), not(classIsInterface()), not(classIsAbstract()));
    private final Map<Class, Class> bindings = new HashMap<Class, Class>();

    @Override
    public String name() {
        return "seed-cli-plugin";
    }

    @Override
    public Collection<ClasspathScanRequest> classpathScanRequests() {
        return classpathScanRequestBuilder().specification(cliHandlerSpec).build();
    }

    @Override
    public InitState init(InitContext initContext) {
        Collection<Class<?>> collection = initContext.scannedTypesBySpecification().get(cliHandlerSpec);

        for (Class<?> class1 : collection) {
            bindings.put(CommandLineHandler.class, class1);

            // for now only one CommandLineHandler
            break;
        }

        return InitState.INITIALIZED;
    }

    @Override
    public Object nativeUnitModule() {
        return new CommandLineModule(bindings);
    }

    private Specification<Class<?>> classIsInterface() {
        return new AbstractSpecification<Class<?>>() {
            @Override
            public boolean isSatisfiedBy(Class<?> candidate) {

                return candidate != null && candidate.isInterface();
            }
        };
    }

    private Specification<Class<?>> classIsAbstract() {
        return new AbstractSpecification<Class<?>>() {
            @Override
            public boolean isSatisfiedBy(Class<?> candidate) {

                return candidate != null && Modifier.isAbstract(candidate.getModifiers());
            }
        };
    }

    private Specification<Class<?>> ancestorImplements(final Class<?> interfaceClass) {
        return new AbstractSpecification<Class<?>>() {

            @Override
            public boolean isSatisfiedBy(Class<?> candidate) {
                if (candidate == null) {
                    return false;
                }

                boolean result = false;

                Class<?>[] allInterfacesAndClasses = getAllInterfacesAndClasses(candidate);

                for (Class<?> clazz : allInterfacesAndClasses) {
                    if (!clazz.isInterface()) {
                        for (Class<?> i : clazz.getInterfaces()) {
                            if (i.equals(interfaceClass)) {
                                result = true;
                                break;
                            }
                        }
                    }
                }

                return result;
            }

        };
    }

    private Class<?>[] getAllInterfacesAndClasses(Class<?> clazz) {
        return getAllInterfacesAndClasses(new Class[]{clazz});
    }

    // This method walks up the inheritance hierarchy to make sure we get every
    // class/interface that could
    // possibly contain the declaration of the annotated method we're looking
    // for.
    @SuppressWarnings("unchecked")
    Class<?>[] getAllInterfacesAndClasses(Class<?>[] classes) {
        if (0 == classes.length) {
            return classes;
        } else {
            List<Class<?>> extendedClasses = new ArrayList<Class<?>>();
            // all interfaces hierarchy
            for (Class<?> clazz : classes) {
                if (clazz != null) {
                    Class<?>[] interfaces = clazz.getInterfaces();
                    if (interfaces != null) {
                        extendedClasses.addAll(Arrays.asList(interfaces));
                    }
                    Class<?> superclass = clazz.getSuperclass();
                    if (superclass != null && superclass != Object.class) {
                        extendedClasses.addAll(Arrays.asList(superclass));
                    }
                }
            }

            // Class::getInterfaces() gets only interfaces/classes
            // implemented/extended directly by a given class.
            // We need to walk the whole way up the tree.
            return (Class[]) ArrayUtils.addAll(classes, getAllInterfacesAndClasses(extendedClasses.toArray(new Class[extendedClasses.size()])));
        }
    }

}
