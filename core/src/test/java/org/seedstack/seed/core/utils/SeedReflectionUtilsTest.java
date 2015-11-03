/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.utils;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matcher;
import org.seedstack.seed.Ignore;
import org.seedstack.seed.Install;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.kametic.specifications.Specification;
import org.seedstack.seed.core.fixtures.DummyService1;
import org.seedstack.seed.core.fixtures.DummyService2;

import java.lang.annotation.*;

/**
 * SeedReflectionUtilsTest
 *
 * @author redouane.loulou@ext.mpsa.com
 */
public class SeedReflectionUtilsTest {
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE})
    @Inherited
    @Ignore
    @interface AnnotationTest {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.METHOD})
    @Inherited
    @Ignore
    @interface AnnotationAllTest {
    }

    @Install
    public static class TestModule1 extends AbstractModule {
        @AnnotationAllTest
        public String foo;
        @AnnotationAllTest
        @Override
        protected void configure() {
        }
        public void bar(){}
    }

    // not automatically installed
    public static class TestModule2 extends AbstractModule {
        @Override
        protected void configure() {
        }
    }

    @AnnotationTest
    public static class TestModule3 extends AbstractModule {
        @Override
        protected void configure() {
        }
    }

    @Test
    public void ancestorMetaAnnotatedWithTest() {
        Matcher matcher = SeedMatchers.ancestorMetaAnnotatedWith(Install.class);
        Assertions.assertThat(matcher).isNotNull();
        Assertions.assertThat(matcher.matches(null)).isFalse();
        Assertions.assertThat(matcher.matches(TestModule1.class)).isTrue();

    }

    @Test
    public void classMetaAnnotatedWithTest() {
        Specification specification = SeedSpecifications.classMetaAnnotatedWith(Install.class);
        Assertions.assertThat(specification.isSatisfiedBy(TestModule1.class)).isTrue();
        Assertions.assertThat(specification.isSatisfiedBy(TestModule2.class)).isFalse();
        Assertions.assertThat(specification.isSatisfiedBy(null)).isFalse();

    }


    @Test
    public void getAllInterfacesAndClassesTest() {
        Class[] hierarchies = SeedReflectionUtils.getAllInterfacesAndClasses(DummyService1.class);
        Assertions.assertThat(hierarchies).isNotNull().isNotEmpty().hasSize(3);
    }

    @Test
    public void getAllInterfacesAndClassesTest2() {
        Class[] hierarchies = SeedReflectionUtils.getAllInterfacesAndClasses(new Class[]{Object.class});
        Assertions.assertThat(hierarchies).isNotNull().isNotEmpty().hasSize(1);
        hierarchies = SeedReflectionUtils.getAllInterfacesAndClasses(new Class[]{DummyService1.class, DummyService2.class});
        Assertions.assertThat(hierarchies).isNotNull().isNotEmpty().hasSize(6);

    }

    @Test
    public void getAnnotationDeepTest() {
        Annotation anno = SeedReflectionUtils.getAnnotationDeep(TestModule1.class.getAnnotation(Install.class), Target.class);
        Assertions.assertThat(anno).isNull();
        anno = SeedReflectionUtils.getAnnotationDeep(TestModule3.class.getAnnotation(AnnotationTest.class), Ignore.class);
        Assertions.assertThat(anno).isNotNull();
        anno = SeedReflectionUtils.getAnnotationDeep(TestModule1.class.getAnnotation(Install.class), Install.class);
        Assertions.assertThat(anno).isEqualTo(TestModule1.class.getAnnotation(Install.class));
    }

    @Test
    public void getMetaAnnotationFromAncestorsTest() {
        Install install = SeedReflectionUtils.getMetaAnnotationFromAncestors(TestModule1.class, Install.class);
        Assertions.assertThat(install).isNotNull();
    }

    @Test
    public void getMethodOrAncestorMetaAnnotatedWith() throws NoSuchMethodException, NoSuchFieldException {
        // Get annotation from declaring class
        Install install0 = SeedReflectionUtils.getMethodOrAncestorMetaAnnotatedWith(TestModule1.class.getDeclaredField("foo"), Install.class);
        Assertions.assertThat(install0).isNotNull();

        // Get annotation from current field
        AnnotationAllTest annotationFieldTest = SeedReflectionUtils.getMethodOrAncestorMetaAnnotatedWith(TestModule1.class.getDeclaredField("foo"), AnnotationAllTest.class);
        Assertions.assertThat(annotationFieldTest).isNotNull();

        // Get annotation from declaring class
        Install install = SeedReflectionUtils.getMethodOrAncestorMetaAnnotatedWith(TestModule1.class.getDeclaredMethod("configure"), Install.class);
        Assertions.assertThat(install).isNotNull();

        // Get annotation from current method
        AnnotationAllTest override = SeedReflectionUtils.getMethodOrAncestorMetaAnnotatedWith(TestModule1.class.getDeclaredMethod("configure"), AnnotationAllTest.class);
        Assertions.assertThat(override).isNotNull();

        AnnotationAllTest result = SeedReflectionUtils.getMethodOrAncestorMetaAnnotatedWith(TestModule1.class.getDeclaredMethod("bar"), AnnotationAllTest.class);
        Assertions.assertThat(result).isNull();

        // Get annotation from current class
        Install install3 = SeedReflectionUtils.getMethodOrAncestorMetaAnnotatedWith(TestModule1.class, Install.class);
        Assertions.assertThat(install3).isNotNull();
    }

    @Test
    public void findCallerTest() {
        StackTraceElement caller = new CallerTestFixture().findCaller();
        Assertions.assertThat(caller).isNotNull();
        Assertions.assertThat(caller.getClassName()).isEqualTo(SeedReflectionUtilsTest.class.getCanonicalName());
        Assertions.assertThat(caller.getMethodName()).isEqualTo("findCallerTest");
    }

}
