/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.internal;

import org.kametic.specifications.AbstractSpecification;
import org.seedstack.seed.core.utils.BaseClassSpecifications;

import javax.ws.rs.Path;

import static org.seedstack.seed.core.utils.BaseClassSpecifications.classAnnotatedWith;
import static org.seedstack.seed.core.utils.BaseClassSpecifications.classIsAbstract;

/**
 * Matches non abstract classes annotated by {@link javax.ws.rs.Path} or containing methods annotated by {@code Path}.
 * It also matches classes extending or implementing a class/interface with a method annotated with JAX-RS annotation.
 * <p>
 * From JAX-RS specification:
 * </p>
 * <blockquote>
 * <b>JAX-RS annotations may be used on the methods and method parameters of a super-class or an implemented
 * interface.</b> Such annotations are inherited by a corresponding sub-class or implementation class method
 * provided that the method and its parameters do not have any JAX-RS annotations of their own. Annotations
 * on a super-class take precedence over those on an implemented interface. The precedence over conflicting
 * annotations defined in multiple implemented interfaces is implementation specific. <b>Note that inheritance of
 * class or interface annotations is not supported.</b>
 * </blockquote>
 *
 * @author pierre.thirouin@ext.mpsa.com (Pierre Thirouin)
 */
public class JaxRsResourceSpecification extends AbstractSpecification<Class<?>> {

    @Override
    public boolean isSatisfiedBy(Class<?> candidate) {
        return classAnnotatedWith(Path.class).and(BaseClassSpecifications.not(classIsAbstract())).isSatisfiedBy(candidate);
    }
}
