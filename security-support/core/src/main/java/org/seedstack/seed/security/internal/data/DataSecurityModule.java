/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.internal.data;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.MapBinder;
import org.seedstack.seed.security.api.data.DataSecurityService;
import org.seedstack.seed.security.spi.data.DataSecurityHandler;
import org.jodah.typetools.TypeResolver;

import java.util.Collection;

/**
 * Configuration Unit regarding Data Security
 *
 * @author epo.jemba@ext.mpsa.com
 * @author pierre.thirouin@ext.mpsa.com
 */
public class DataSecurityModule extends AbstractModule {
	private static final TypeLiteral< DataSecurityHandler<?>> MAP_TYPE_LITERAL = new TypeLiteral< DataSecurityHandler<?>> (){};
	private Collection<Class<? extends DataSecurityHandler<?>>> dataSecurityHandlers;

    /**
     * Constructor.
     *
     * @param dataSecurityHandlers list of security handler
     */
    public DataSecurityModule(Collection<Class<? extends DataSecurityHandler<?>>> dataSecurityHandlers) {
        this.dataSecurityHandlers = dataSecurityHandlers;
    }


    @Override
	protected void configure() {

		bind(DataSecurityService.class).to(DataSecurityServiceInternal.class);

		MapBinder<Object , DataSecurityHandler<?>> mapBinder = MapBinder.newMapBinder(binder(), TypeLiteral.get(Object.class), MAP_TYPE_LITERAL);

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
	}


}
