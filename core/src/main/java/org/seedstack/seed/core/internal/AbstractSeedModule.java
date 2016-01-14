/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
/**
 * 
 */
package org.seedstack.seed.core.internal;

import com.google.common.collect.Multimap;
import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import org.seedstack.seed.core.utils.SeedReflectionUtils;

import javax.inject.Qualifier;
import java.lang.annotation.Annotation;
import java.util.Map.Entry;

/**
 * This abstract module provides helper methods to address complex bindings.
 * 
 * @author redouane.loulou@ext.mpsa.com
 * @author pierre.thirouin@ext.mpsa.com
 */
public abstract class AbstractSeedModule extends AbstractModule {

    /**
     * Binds typeLiterals to their implementations using the qualifier when it exists.
     * <p>
     * For instance:
     * <pre>
     * class MyClassA extends MySuperClass&lt;TypeGenericA&gt; {...}
     *
     * {@literal @}Inject
     * MySuperClass&lt;TypeGenericA&gt; &lt;- MyClassA
     *
     * class MyClassB extends MySuperClass&lt;TypeGenericB&gt; {...}
     *
     * {@literal @}Inject
     * MySuperClass&lt;TypeGenericB&gt; &lt;- MyClassB
     * </pre>
     *
     * If a {@link javax.inject.Qualifier} is specified on the implementation, it will
     * be used in the bound {@link com.google.inject.Key}.
     * <p>
     * For instance, if {@code MyClassC} is annotated by a qualifier:
     * <pre>
     * {@literal @}Named("C")
     * class MyClassC extends MySuperClass&lt;TypeGenericA&gt; {
     *     ...
     * }
     * </pre>
     * Then, the qualifier should be used specified at the injection point.
     * <pre>
     * {@literal @}Inject @Named("C")
     * MySuperClass&lt;TypeGenericA&gt; &lt;- MyClassC
     * </pre>
     *
     * @param bindingsMap classes to bind
     */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void bindTypeLiterals(Multimap<TypeLiteral<?>, Class<?>> bindingsMap) {
		for (Entry<TypeLiteral<?>, Class<?>> entry : bindingsMap.entries()) {
			TypeLiteral<?> typeLiteral = entry.getKey();
			Class classToBind = entry.getValue();
			Annotation annotation = SeedReflectionUtils.getAnnotationMetaAnnotatedFromAncestor(classToBind, Qualifier.class);
			if (annotation != null) {
				bind(Key.get(typeLiteral, annotation)).to(classToBind);
			} else {
				bind(typeLiteral).to(classToBind);
			}
		}
	}

}
