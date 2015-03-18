/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.spring.dao;

import java.util.List;

import org.hibernate.SessionFactory;
import org.seedstack.seed.spring.model.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implements the data access methods for Customer persistence
 *
 * @author shaines
 */

@Repository( "customerDao" )
@Transactional(propagation = Propagation.MANDATORY)
public class CustomerDaoImpl implements CustomerDao
{
    @Autowired
    private SessionFactory sessionFactory;

    @Override
    public Customer findById( long id )
    {
        return ( Customer )sessionFactory.getCurrentSession().get(Customer.class, id);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Customer> findAll()
    {
        return sessionFactory.getCurrentSession().createQuery("from org.seedstack.seed.spring.model.Customer").list();
    }

    @Override
    public void save( Customer customer )
    {
        sessionFactory.getCurrentSession().save(customer);
    }

    @Override
    public void update( Customer customer )
    {
        sessionFactory.getCurrentSession().update(customer);
    }

    @Override
    public void delete( Customer customer )
    {
        sessionFactory.getCurrentSession().delete( customer );
    }




}
