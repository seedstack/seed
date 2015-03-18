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

import org.seedstack.seed.spring.model.Customer;

/**
 * Defines the data access methods for Customer persistence
 *
 * @author shaines
 */
public interface CustomerDao
{
    public Customer findById( long id );
    public List<Customer> findAll();
    public void save( Customer customer );
    public void update( Customer customer );
    public void delete( Customer customer );
}
