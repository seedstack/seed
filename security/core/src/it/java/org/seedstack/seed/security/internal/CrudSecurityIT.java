/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.internal;

import org.apache.shiro.SecurityUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.seedstack.seed.it.SeedITRunner;
import org.seedstack.seed.security.AuthorizationException;
import org.seedstack.seed.security.SecuritySupport;
import org.seedstack.seed.security.WithUser;
import org.seedstack.seed.security.internal.fixtures.AnnotatedCrudClass4Security;
import org.seedstack.seed.security.internal.fixtures.AnnotatedCrudMethods4Security;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@RunWith(SeedITRunner.class)
public class CrudSecurityIT {

    @Inject
    private AnnotatedCrudClass4Security annotatedClass;

    @Inject
    private AnnotatedCrudMethods4Security annotatedMethods;

    @Inject
    private SecuritySupport securitySupport;

    @Test
    @WithUser(id = "Obiwan", password = "yodarulez")
    public void Obiwan_should_not_be_able_to_interpret_anything() {
        assertThatThrownBy(() -> annotatedClass.read()).isInstanceOf(AuthorizationException.class);
        assertThatThrownBy(() -> annotatedMethods.read()).isInstanceOf(AuthorizationException.class);

    }

    // RESTBOT
    @Test
    @WithUser(id = "R2D2", password = "beep")
    public void r2d2_should_be_able_to_be_a_restbot() {
        // Delete jabba!
        assertThat(SecurityUtils.getSubject().isPermitted("jabba:delete")).isTrue();
        assertThat(securitySupport.isPermitted("jabba:delete")).isTrue();

        // Update c3p0!
        assertThat(SecurityUtils.getSubject().isPermitted("c3p0:update")).isTrue();
        assertThat(securitySupport.isPermitted("c3p0:update")).isTrue();

        // Create X-Wing
        assertThat(SecurityUtils.getSubject().isPermitted("xwing:create")).isTrue();
        assertThat(securitySupport.isPermitted("xwing:create")).isTrue();

        // Read chewaka
        assertThat(SecurityUtils.getSubject().isPermitted("chewaka:read")).isTrue();
        assertThat(securitySupport.isPermitted("chewaka:read")).isTrue();

        // is a restbot
        assertThat(SecurityUtils.getSubject().hasRole("restbot")).isTrue();
        assertThat(securitySupport.hasRole("restbot")).isTrue();
    }

    @Test
    @WithUser(id = "R2D2", password = "beep")
    public void r2d2_should_be_able_to_call_any_kind_of_method() {

        assertThat(annotatedClass.create()).isTrue();
        assertThat(annotatedClass.read()).isTrue();
        assertThat(annotatedClass.update()).isTrue();
        assertThat(annotatedClass.delete()).isTrue();

        assertThat(annotatedMethods.create()).isTrue();
        assertThat(annotatedMethods.read()).isTrue();
        assertThat(annotatedMethods.update()).isTrue();
        assertThat(annotatedMethods.delete()).isTrue();

    }

    // INTERPRETER
    @Test
    @WithUser(id = "C3P0", password = "ewokgod")
    public void c3p0_should_only_be_able_to_read_rest_as_interpreter() {
        // Read ewoks
        assertThat(SecurityUtils.getSubject().isPermitted("ewok:read")).isTrue();
        assertThat(securitySupport.isPermitted("ewok:read")).isTrue();

        // Should not be able to update itself
        assertThat(SecurityUtils.getSubject().isPermitted("c3p0:update")).isFalse();
        assertThat(securitySupport.isPermitted("c3p0:update")).isFalse();

        // Is an interpreter
        assertThat(SecurityUtils.getSubject().hasRole("interpreter")).isTrue();
        assertThat(securitySupport.hasRole("interpreter")).isTrue();
    }

    @Test
    @WithUser(id = "C3P0", password = "ewokgod")
    public void c3p0_should_be_able_to_read_data() {

        assertThatThrownBy(() -> annotatedMethods.create()).isInstanceOf(AuthorizationException.class);
        assertThatThrownBy(() -> annotatedMethods.update()).isInstanceOf(AuthorizationException.class);
        assertThatThrownBy(() -> annotatedMethods.delete()).isInstanceOf(AuthorizationException.class);
        assertThat(annotatedMethods.read()).isTrue();

        assertThatThrownBy(() -> annotatedClass.create()).isInstanceOf(AuthorizationException.class);
        assertThatThrownBy(() -> annotatedClass.update()).isInstanceOf(AuthorizationException.class);
        assertThatThrownBy(() -> annotatedClass.delete()).isInstanceOf(AuthorizationException.class);
        assertThat(annotatedClass.read()).isTrue();
    }
}
