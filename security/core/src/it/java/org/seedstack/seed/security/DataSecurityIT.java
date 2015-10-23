/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security;

import com.google.common.collect.Lists;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.seedstack.seed.it.SeedITRunner;
import org.seedstack.seed.security.api.WithUser;
import org.seedstack.seed.security.api.data.DataSecurityService;
import org.seedstack.seed.security.fixtures.data.Dummy;
import org.seedstack.seed.security.fixtures.data.DummyFactory;
import org.seedstack.seed.security.fixtures.data.DummyService;
import org.seedstack.seed.security.fixtures.data.MyUnsecuredPojo;
import org.seedstack.seed.security.fixtures.data.SuperSecretPojo;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author epo.jemba@ext.mpsa.com
 */
@RunWith(SeedITRunner.class)
public class DataSecurityIT {
    @Inject
    DataSecurityService dataSecurityService;

    @Inject
    DummyService dummyService;

    @Test
    @WithUser(id = "Anakin", password = "imsodark")
    public void checking_data_security_interceptions_patterns() {
        Dummy dummy11 = DummyFactory.create(11);
        Dummy dummy12 = DummyFactory.create(12);
        Dummy dummy13 = DummyFactory.create(13);
        Dummy dummy21 = DummyFactory.create(21);

        Dummy dummyReturn1 = dummyService.service1(dummy11, dummy12, dummy13);

        Dummy dummyReturn2 = dummyService.service2(dummy21);

        assertDummySecured(1, dummyReturn1);
        assertDummy(2, dummyReturn2);

        assertDummySecured(12, dummy12);
        assertDummy(13, dummy13);
        assertDummySecured(11, dummy11);
    }

    void assertDummy(Integer i, Dummy d) {
        Assertions.assertThat(d.getDummy1()).isEqualTo("dummy1-" + i);
        Assertions.assertThat(d.getDummy2()).isEqualTo(Long.valueOf(i));
        Assertions.assertThat(d.getDummy3()).isTrue();
        Assertions.assertThat(d.getDummy4()).isEqualTo("dummy4-" + i);
    }

    void assertDummySecured(Integer i, Dummy d) {
        Assertions.assertThat(d.getDummy1()).isEqualTo("obfuscated!!");
        Assertions.assertThat(d.getDummy2()).isEqualTo(Long.valueOf(0));
        Assertions.assertThat(d.getDummy3()).isFalse();
        Assertions.assertThat(d.getDummy4()).isEqualTo("");
    }

    @Test
    public void testSecure() {
        Assertions.assertThat(dataSecurityService).isNotNull();
        Assertions.assertThat(dataSecurityService.getClass()).isNotNull();
    }

    @Test
    @WithUser(id = "Anakin", password = "imsodark")
    public void testNullValues() {
        SuperSecretPojo superPojo = new SuperSecretPojo();

        dataSecurityService.secure(superPojo);

        assertThat(superPojo.getMyint()).isEqualTo(0);
        assertThat(superPojo.getMyInteger()).isNull();
        assertThat(superPojo.getMyshort()).isEqualTo((short) 0);
        assertThat(superPojo.getMyShort()).isNull();
        assertThat(superPojo.isMyboolean()).isFalse();
        assertThat(superPojo.getMyBoolean()).isNull();
        assertThat(superPojo.getMybyte()).isEqualTo((byte) 0);
        assertThat(superPojo.getMyByte()).isNull();
        assertThat(superPojo.getMylong()).isEqualTo(0l);
        assertThat(superPojo.getMyLong()).isNull();
        assertThat(superPojo.getMyfloat()).isEqualTo(0f);
        assertThat(superPojo.getMyFloat()).isNull();
        assertThat(superPojo.getMydouble()).isEqualTo(0d);
        assertThat(superPojo.getMyDouble()).isNull();
        assertThat(superPojo.getMycharacter()).isEqualTo((char) 0);
        assertThat(superPojo.getMyCharacter()).isNull();
        assertThat(superPojo.getMyString()).isNull();
    }

    @Test
    @WithUser(id = "Anakin", password = "imsodark")
    public void test() {
        MyUnsecuredPojo pojo = new MyUnsecuredPojo("Doe", "john", "password", 12345);
        MyUnsecuredPojo pojo2 = new MyUnsecuredPojo("Doe", "jane", "password", 12345);

        dataSecurityService.secure(Lists.newArrayList(pojo, pojo2));

        assertThat(pojo.getName()).isEqualTo("D.");
        assertThat(pojo.getSalary()).isEqualTo(12000);
        assertThat(pojo.getPassword()).isNull();
    }


}
