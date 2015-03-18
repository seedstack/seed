/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
/**
 * 
 */
package org.seedstack.seed.spring;

import javax.inject.Inject;
import javax.inject.Named;

import org.seedstack.seed.spring.api.SpringTransactionManager;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.transaction.IllegalTransactionStateException;

import org.seedstack.seed.it.SeedITRunner;
import org.seedstack.seed.spring.dao.CustomerDao;
import org.seedstack.seed.spring.model.Customer;
import org.seedstack.seed.transaction.api.Transactional;

/**
 * SpringTransactionHandlerIT
 * 
 * @author redouane.loulou@ext.mpsa.com
 * 
 */
@RunWith(SeedITRunner.class)
public class SpringTransactionHandlerIT {

	@Inject
	@Named("customerDao")
	CustomerDao customerDao;

	@Test
	@Transactional
    @SpringTransactionManager("transactionManager")
	public void testTransactional() {
		Assertions.assertThat(customerDao).isNotNull();
		Customer customer = new Customer("john", "doe", "john.doe@gmail.com",
				null);
		customerDao.save(customer);
		customerDao.delete(customer);
		Assertions.assertThat(customer).isNotNull();
	}

	@Test(expected = RuntimeException.class)
	@Transactional
	public void testRollback() {
		Assertions.assertThat(customerDao).isNotNull();
		Customer customer = new Customer("john", "doe", "john.doe@gmail.com",
				null);
		customerDao.save(customer);
		customerDao.delete(customer);
		Assertions.assertThat(customer).isNotNull();
		throw new RuntimeException("testRollback");
	}
	
	@Test(expected = IllegalTransactionStateException.class)
	public void testUnmanagedTransactionalComponent() {
		Assertions.assertThat(customerDao).isNotNull();
		Customer customer = new Customer("john", "doe", "john.doe@gmail.com",
				null);
		customerDao.save(customer);
		customerDao.delete(customer);
		Assertions.assertThat(customer).isNotNull();
	}

}
