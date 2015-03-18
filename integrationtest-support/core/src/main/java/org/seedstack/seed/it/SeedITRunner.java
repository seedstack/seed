/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.it;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.ProvisionException;
import org.seedstack.seed.core.api.SeedException;
import org.seedstack.seed.core.utils.SeedReflectionUtils;
import org.seedstack.seed.it.api.Expect;
import org.seedstack.seed.it.api.WithPlugins;
import org.seedstack.seed.it.api.WithoutSpiPluginsLoader;
import org.seedstack.seed.it.internal.ITErrorCode;
import org.seedstack.seed.it.internal.ITPlugin;
import org.seedstack.seed.it.spi.ITKernelMode;
import org.seedstack.seed.it.spi.ITRunnerPlugin;
import io.nuun.kernel.api.Kernel;
import io.nuun.kernel.api.Plugin;
import io.nuun.kernel.api.config.KernelConfiguration;
import io.nuun.kernel.core.AbstractPlugin;
import io.nuun.kernel.core.NuunCore;
import org.junit.rules.MethodRule;
import org.junit.rules.TestRule;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import static io.nuun.kernel.core.NuunCore.createKernel;


/**
 * This runner can be used to run JUnit tests with SEED integration. Tests
 * launched with this runner will benefit from SEED features (injection, aop
 * interception, test extensions, ...).
 *
 * @author redouane.loulou@ext.mpsa.com
 * @author yves.dautremay@mpsa.com
 * @author epo.jemba@ext.mpsa.com
 * @author adrien.lauer@mpsa.com
 */
public class SeedITRunner extends BlockJUnit4ClassRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(SeedITRunner.class);

    private final boolean safeModeRequired;
    private final Class<? extends Plugin>[] safeModePlugins;
    private final Module[] safeModeModules;
    private final ServiceLoader<ITRunnerPlugin> plugins;
    private final Class<?> expectedClass;

    private ITKernelMode kernelMode = ITKernelMode.ANY;
    private Kernel kernel;
    private Exception safeModeException;

    /**
     * Creates the runner for the corresponding test class.
     *
     * @param klass the test class.
     * @throws InitializationError if an initialization error occurs.
     */
    @SuppressWarnings("unchecked")
    public SeedITRunner(Class<?> klass) throws InitializationError {
        super(klass);
        this.safeModeRequired = false;
        this.safeModePlugins = new Class[0];
        this.safeModeModules = new Module[0];
        this.plugins = ServiceLoader.load(ITRunnerPlugin.class, SeedReflectionUtils.findMostCompleteClassLoader(SeedITRunner.class));
        this.expectedClass = findExpectedClass();
    }

    protected SeedITRunner(Class<?> klass, Class<? extends Plugin> safeModePlugins[], Module... safeModeModules) throws InitializationError {
        super(klass);
        this.safeModeRequired = true;
        this.safeModePlugins = safeModePlugins.clone();
        this.safeModeModules = safeModeModules.clone();
        this.plugins = ServiceLoader.load(ITRunnerPlugin.class, SeedReflectionUtils.findMostCompleteClassLoader(SeedITRunner.class));
        this.expectedClass = findExpectedClass();
    }

    @Override
    public void run(RunNotifier notifier) {
        // Determine kernel mode to use and gather configuration
        for (ITRunnerPlugin plugin : plugins) {
            Map<String, String> pluginDefaultConfiguration = plugin.provideDefaultConfiguration(getTestClass());
            if (pluginDefaultConfiguration != null) {
                for (Map.Entry<String, String> entry : pluginDefaultConfiguration.entrySet()) {
                    System.setProperty(ITPlugin.DEFAULT_CONFIGURATION_PREFIX + entry.getKey(), entry.getValue());
                }
            }

            switch (plugin.kernelMode(getTestClass())) {
                case NONE:
                    if (kernelMode == ITKernelMode.PER_TEST_CLASS || kernelMode == ITKernelMode.PER_TEST) {
                        throw SeedException.createNew(ITErrorCode.TEST_PLUGINS_MISMATCH).put("requestedKernelMode", ITKernelMode.NONE).put("incompatibleKernelMode", kernelMode.toString()).put("testClass", getTestClass().getJavaClass().getCanonicalName());
                    }
                    kernelMode = ITKernelMode.NONE;
                    break;
                case PER_TEST_CLASS:
                    if (kernelMode == ITKernelMode.NONE || kernelMode == ITKernelMode.PER_TEST) {
                        throw SeedException.createNew(ITErrorCode.TEST_PLUGINS_MISMATCH).put("requestedKernelMode", ITKernelMode.PER_TEST_CLASS).put("incompatibleKernelMode", kernelMode.toString()).put("testClass", getTestClass().getJavaClass().getCanonicalName());
                    }
                    kernelMode = ITKernelMode.PER_TEST_CLASS;
                    break;
                case PER_TEST:
                    if (kernelMode == ITKernelMode.NONE || kernelMode == ITKernelMode.PER_TEST_CLASS) {
                        throw SeedException.createNew(ITErrorCode.TEST_PLUGINS_MISMATCH).put("requestedKernelMode", ITKernelMode.PER_TEST).put("incompatibleKernelMode", kernelMode.toString()).put("testClass", getTestClass().getJavaClass().getCanonicalName());
                    }
                    kernelMode = ITKernelMode.PER_TEST;
                    break;
                default:
                    break;
            }
        }

        // Default kernel mode is per test class
        if (kernelMode == ITKernelMode.ANY) {
            kernelMode = ITKernelMode.PER_TEST_CLASS;
        }

        LOGGER.info("Kernel mode is {}", kernelMode);

        if (kernelMode == ITKernelMode.PER_TEST_CLASS) {
            initKernel();
        }

        RunNotifier transformedNotifier = transformNotifier(notifier, safeModeException);

        beforeClassTest(transformedNotifier, safeModeException);
        if (safeModeException == null) {
            super.run(transformedNotifier);
        }
        afterClassTest(transformedNotifier, safeModeException);

        if (kernelMode == ITKernelMode.PER_TEST_CLASS) {
            stopKernel();
        }

        if (safeModeException != null) {
            processException(safeModeException);
        }
    }

    /**
     * Can be used by subclasses to alter the run notifier.
     *
     * @param notifier          the initial notifier
     * @param eventualException exception if kernel failed to initialize normally. A safe mode kernel is still available.
     * @return an adapted notifier
     */
    protected RunNotifier transformNotifier(RunNotifier notifier, Exception eventualException) {
        return notifier;
    }

    /**
     * afterClassTest
     *
     * @param notifier          RunNotifier
     * @param eventualException exception if kernel failed to initialize normally. A safe mode kernel is still available.
     * @author redouane.loulou@ext.mpsa.com
     */
    protected void afterClassTest(RunNotifier notifier, Exception eventualException) {
        // can be overridden by subclasses
    }

    /**
     * beforeClassTest
     *
     * @param notifier          RunNotifier
     * @param eventualException exception if kernel failed to initialize normally. A safe mode kernel is still available.
     * @author redouane.loulou@ext.mpsa.com
     */
    protected void beforeClassTest(RunNotifier notifier, Exception eventualException) {
        // can be overridden by subclasses
    }

    /**
     * @return the active kernel (may be a safe kernel if main kernel failed to start)
     */
    protected Kernel getKernel() {
        return this.kernel;
    }

    @Override
    protected List<TestRule> getTestRules(Object target) {
        List<TestRule> testRules = super.getTestRules(target);
        for (ITRunnerPlugin plugin : plugins) {
            for (Class<? extends TestRule> testRuleClass : plugin.provideTestRulesToApply(getTestClass(), target)) {
                try {
                    testRules.add(instantiate(testRuleClass));
                } catch (Exception e) {
                    throw SeedException.wrap(e, ITErrorCode.FAILED_TO_INSTANTIATE_TEST_RULE).put("class", testRuleClass);
                }
            }
        }
        return testRules;
    }

    @Override
    protected List<TestRule> classRules() {
        List<TestRule> testRules = super.classRules();
        for (ITRunnerPlugin plugin : plugins) {
            for (Class<? extends TestRule> testRuleClass : plugin.provideClassRulesToApply(getTestClass())) {
                try {
                    testRules.add(instantiate(testRuleClass));
                } catch (Exception e) {
                    throw SeedException.wrap(e, ITErrorCode.FAILED_TO_INSTANTIATE_TEST_RULE).put("class", testRuleClass);
                }
            }
        }
        return testRules;
    }

    @Override
    protected List<MethodRule> rules(Object target) {
        List<MethodRule> methodRules = super.rules(target);
        for (ITRunnerPlugin plugin : plugins) {
            for (Class<? extends MethodRule> methodRuleClass : plugin.provideMethodRulesToApply(getTestClass(), target)) {
                try {
                    methodRules.add(instantiate(methodRuleClass));
                } catch (Exception e) {
                    throw SeedException.wrap(e, ITErrorCode.FAILED_TO_INSTANTIATE_TEST_RULE).put("class", methodRuleClass);
                }
            }
        }
        return methodRules;
    }

    @Override
    protected Object createTest() throws Exception {
        Object test;

        try {
            test = instantiate(getTestClass().getJavaClass());
        } catch (Exception e) { // NOSONAR
            processException(e);
            test = super.createTest();
        }

        return test;
    }

    @Override
    protected void runChild(FrameworkMethod method, RunNotifier notifier) {
        if (kernelMode == ITKernelMode.PER_TEST) {
            initKernel();
        }

        super.runChild(method, notifier);

        if (kernelMode == ITKernelMode.PER_TEST) {
            stopKernel();
        }
    }

    private <T> T instantiate(Class<T> toInstantiate) throws IllegalAccessException, InstantiationException {
        if (kernel != null) {
            return kernel.objectGraph().as(Injector.class).getInstance(toInstantiate);
        } else {
            return toInstantiate.newInstance();
        }
    }

    private void initKernel() {
        safeModeException = null;
        try {
            KernelConfiguration kernelConfiguration = NuunCore.newKernelConfiguration();
            withoutSpiPluginsLoader(kernelConfiguration);
            withPlugins(kernelConfiguration);
            kernelConfiguration.param(ITPlugin.IT_CLASS_NAME, getTestClass().getJavaClass().getName());

            Kernel normalKernel = createKernel(kernelConfiguration);
            normalKernel.init();
            normalKernel.start();
            kernel = normalKernel;
        } catch (Exception e) { // NOSONAR
            if (safeModeRequired) {
                LOGGER.error("Failed to start kernel in normal mode, using safe mode to complete the testing process");
                safeModeException = e;
                try {
                    KernelConfiguration safeModeKernelConfiguration = NuunCore.newKernelConfiguration();

                    safeModeKernelConfiguration.withoutSpiPluginsLoader();
                    safeModeKernelConfiguration.plugins(safeModePlugins);
                    safeModeKernelConfiguration.plugins(new InternalPlugin(new InternalModule(safeModeModules)));
                    safeModeKernelConfiguration.param(ITPlugin.IT_CLASS_NAME, getTestClass().getJavaClass().getName());

                    Kernel safeKernel = createKernel(safeModeKernelConfiguration);
                    safeKernel.init();
                    safeKernel.start();
                    kernel = safeKernel;
                } catch (Exception e2) {
                    e2.initCause(e);
                    throw SeedException.wrap(e2, ITErrorCode.FAILED_TO_INITIALIZE_SAFE_KERNEL);
                }
            } else {
                try {
                    processException(e);
                } catch (SeedException e2) {
                    throw SeedException.wrap(e2, ITErrorCode.FAILED_TO_INITIALIZE_KERNEL);
                }
            }
        }

        if (kernel != null) {
            kernel.objectGraph().as(Injector.class).injectMembers(this);
        }
    }

    private void stopKernel() {
        try {
            if (kernel != null) {
                kernel.stop();
            }
        } finally {
            kernel = null;
        }
    }

    private void processException(Exception e) {
        Throwable unwrappedThrowable = e;

        // Unwrap known Guice exceptions to access the real one
        if (unwrappedThrowable != null && ProvisionException.class.isAssignableFrom(e.getClass()) && e.getCause() != null) {
            unwrappedThrowable = e.getCause();
        }

        if (expectedClass == null && unwrappedThrowable != null) {
            throw SeedException.wrap(unwrappedThrowable, ITErrorCode.UNEXPECTED_EXCEPTION_OCCURRED).put("occurredClass", unwrappedThrowable.getClass());
        }

        if (expectedClass != null) {
            if (unwrappedThrowable == null) {
                throw SeedException.createNew(ITErrorCode.EXPECTED_EXCEPTION_DID_NOT_OCCURRED).put("expectedClass", expectedClass);
            } else if (!unwrappedThrowable.getClass().equals(expectedClass)) {
                throw SeedException.createNew(ITErrorCode.ANOTHER_EXCEPTION_THAN_EXPECTED_OCCURED).put("expectedClass", expectedClass).put("occurredClass", unwrappedThrowable.getClass());
            }
        }
    }

    private Class<? extends Exception> findExpectedClass() {
        Expect annotation = getTestClass().getJavaClass().getAnnotation(Expect.class);
        if (annotation != null) {
            return annotation.value();
        } else {
            return null;
        }
    }

    private void withPlugins(KernelConfiguration kernelConfiguration) {
        WithPlugins annotation = getTestClass().getJavaClass().getAnnotation(WithPlugins.class);
        if (annotation != null) {
            kernelConfiguration.plugins(annotation.value());
        }
    }

    private void withoutSpiPluginsLoader(KernelConfiguration kernelConfiguration) {
        if (getTestClass().getJavaClass().getAnnotation(WithoutSpiPluginsLoader.class) != null) {
            kernelConfiguration.withoutSpiPluginsLoader();
        }
    }

    private static final class InternalModule extends AbstractModule {
        private Module[] internalModules;

        private InternalModule(Module[] modules) {
            internalModules = modules.clone();
        }

        @Override
        protected void configure() {
            if (internalModules != null) {
                for (Module m : internalModules) {
                    install(m);
                }
            }
        }
    }

    private static final class InternalPlugin extends AbstractPlugin {
        private Module module;

        private InternalPlugin(Module module) {
            this.module = module;
        }

        @Override
        public String name() {
            return "fixture-internal-plugin";
        }

        @Override
        public Object nativeUnitModule() {
            return module;
        }
    }
}
