/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.it;

import com.google.inject.Injector;
import com.google.inject.ProvisionException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.nuun.kernel.api.Kernel;
import io.nuun.kernel.api.config.KernelConfiguration;
import io.nuun.kernel.core.NuunCore;
import org.junit.Before;
import org.junit.rules.MethodRule;
import org.junit.rules.TestRule;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runner.notification.StoppedByUserException;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.core.Seed;
import org.seedstack.seed.core.internal.configuration.ConfigurationPlugin;
import org.seedstack.seed.it.internal.ITErrorCode;
import org.seedstack.seed.it.internal.ITPlugin;
import org.seedstack.seed.it.spi.ITKernelMode;
import org.seedstack.seed.it.spi.ITRunnerPlugin;
import org.seedstack.seed.it.spi.KernelRule;
import org.seedstack.seed.it.spi.PausableStatement;
import org.seedstack.shed.ClassLoaders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;


/**
 * This runner can be used to run JUnit tests with Seed integration. Tests
 * launched with this runner will benefit from Seed features (injection, aop
 * interception, test extensions, ...).
 */
public class SeedITRunner extends BlockJUnit4ClassRunner {
    private final ServiceLoader<ITRunnerPlugin> plugins;
    private final Expect.TestingStep expectedTestingStep;
    private final Class<? extends Throwable> expectedClass;

    private ITKernelMode kernelMode = ITKernelMode.ANY;
    private Map<String, String> defaultConfiguration;
    private Kernel kernel;

    /**
     * Creates the runner for the corresponding test class.
     *
     * @param klass the test class.
     * @throws InitializationError if an initialization error occurs.
     */
    @SuppressWarnings("unchecked")
    public SeedITRunner(Class<?> klass) throws InitializationError {
        super(klass);
        this.plugins = ServiceLoader.load(ITRunnerPlugin.class, ClassLoaders.findMostCompleteClassLoader(SeedITRunner.class));

        Expect annotation = getTestClass().getJavaClass().getAnnotation(Expect.class);
        if (annotation != null) {
            this.expectedClass = annotation.value();
            this.expectedTestingStep = annotation.step();
        } else {
            this.expectedClass = null;
            this.expectedTestingStep = null;
        }
    }

    protected void collectInitializationErrors(List<Throwable> errors) {
        super.collectInitializationErrors(errors);

        validatePublicVoidNoArgMethods(BeforeKernel.class, true, errors);
        validatePublicVoidNoArgMethods(AfterKernel.class, true, errors);
    }

    @Override
    public void run(RunNotifier notifier) {
        try {
            // Determine kernel mode to use
            for (ITRunnerPlugin plugin : plugins) {
                ITKernelMode pluginKernelMode = plugin.kernelMode(getTestClass());
                if (pluginKernelMode == null) {
                    pluginKernelMode = ITKernelMode.ANY;
                }

                switch (pluginKernelMode) {
                    case NONE:
                        if (kernelMode == ITKernelMode.PER_TEST_CLASS || kernelMode == ITKernelMode.PER_TEST) {
                            throw SeedException.createNew(ITErrorCode.TEST_PLUGINS_MISMATCH)
                                    .put("requestedKernelMode", ITKernelMode.NONE)
                                    .put("incompatibleKernelMode", kernelMode.toString())
                                    .put("testClass", getTestClass().getJavaClass().getCanonicalName());
                        }
                        kernelMode = ITKernelMode.NONE;
                        break;
                    case PER_TEST_CLASS:
                        if (kernelMode == ITKernelMode.NONE || kernelMode == ITKernelMode.PER_TEST) {
                            throw SeedException.createNew(ITErrorCode.TEST_PLUGINS_MISMATCH)
                                    .put("requestedKernelMode", ITKernelMode.PER_TEST_CLASS)
                                    .put("incompatibleKernelMode", kernelMode.toString())
                                    .put("testClass", getTestClass().getJavaClass().getCanonicalName());
                        }
                        kernelMode = ITKernelMode.PER_TEST_CLASS;
                        break;
                    case PER_TEST:
                        if (kernelMode == ITKernelMode.NONE || kernelMode == ITKernelMode.PER_TEST_CLASS) {
                            throw SeedException.createNew(ITErrorCode.TEST_PLUGINS_MISMATCH)
                                    .put("requestedKernelMode", ITKernelMode.PER_TEST)
                                    .put("incompatibleKernelMode", kernelMode.toString())
                                    .put("testClass", getTestClass().getJavaClass().getCanonicalName());
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

            if (kernelMode == ITKernelMode.PER_TEST_CLASS) {
                initKernel(gatherConfiguration(null));
            }

            super.run(notifier);

            if (kernelMode == ITKernelMode.PER_TEST_CLASS) {
                stopKernel();
            }
        } catch (StoppedByUserException e) {
            throw e;
        } catch (Throwable t) {
            notifyFailure(t, notifier);
        }
    }

    /**
     * @return the active kernel
     */
    protected Kernel getKernel() {
        return this.kernel;
    }

    @Override
    protected List<TestRule> getTestRules(Object target) {
        List<TestRule> testRules = super.getTestRules(target);
        for (ITRunnerPlugin plugin : plugins) {
            testRules.addAll(buildRules(plugin.provideTestRulesToApply(getTestClass(), target)));
        }
        return testRules;
    }

    @Override
    protected List<TestRule> classRules() {
        List<TestRule> classRules = super.classRules();
        for (ITRunnerPlugin plugin : plugins) {
            classRules.addAll(buildRules(plugin.provideClassRulesToApply(getTestClass())));
        }
        return classRules;
    }

    @Override
    protected List<MethodRule> rules(Object target) {
        List<MethodRule> methodRules = super.rules(target);
        for (ITRunnerPlugin plugin : plugins) {
            methodRules.addAll(buildRules(plugin.provideMethodRulesToApply(getTestClass(), target)));
        }
        return methodRules;
    }

    @SuppressFBWarnings(value = "REC_CATCH_EXCEPTION", justification = "We want to catch all exception to wrap them")
    private <T> List<T> buildRules(List<Class<? extends T>> rulesToApply) {
        List<T> rules = new ArrayList<>();
        if (rulesToApply != null) {
            for (Class<? extends T> ruleClass : rulesToApply) {
                try {
                    T ruleInstance = instantiate(ruleClass);
                    if (ruleInstance instanceof KernelRule) {
                        ((KernelRule) ruleInstance).acceptKernelConfiguration(provideKernelConfiguration(defaultConfiguration));
                    }
                    rules.add(ruleInstance);
                } catch (Exception e) {
                    throw SeedException.wrap(e, ITErrorCode.FAILED_TO_INSTANTIATE_TEST_RULE).put("ruleClass", ruleClass.getCanonicalName());
                }
            }
        }
        return rules;
    }

    @Override
    protected Statement withBefores(FrameworkMethod method, Object target, Statement statement) {
        List<FrameworkMethod> befores = getTestClass().getAnnotatedMethods(Before.class);
        return befores.isEmpty() ? statement : new PausableStatement(statement, befores, target);
    }

    @Override
    protected Object createTest() throws Exception {
        Exception eventualException = null;
        Object test = null;

        try {
            test = instantiate(getTestClass().getJavaClass());
        } catch (Exception e) {
            eventualException = e;
        }

        processException(eventualException, Expect.TestingStep.INSTANTIATION);

        // we still want a non-injected test instance to verify that things have failed
        if (test == null) {
            test = super.createTest();
        }

        return test;
    }

    @Override
    protected void runChild(FrameworkMethod method, RunNotifier notifier) {
        try {
            defaultConfiguration = gatherConfiguration(method);

            if (kernelMode == ITKernelMode.PER_TEST) {
                initKernel(gatherConfiguration(method));
            }

            super.runChild(method, notifier);

            if (kernelMode == ITKernelMode.PER_TEST) {
                stopKernel();
            }
        } catch (Throwable t) {
            notifyFailure(t, notifier);
        }
    }

    private <T> T instantiate(Class<T> toInstantiate) throws IllegalAccessException, InstantiationException {
        if (kernel != null && kernel.isStarted()) {
            return kernel.objectGraph().as(Injector.class).getInstance(toInstantiate);
        } else {
            return toInstantiate.newInstance();
        }
    }

    /**
     * Initialize the kernel with the specified configuration.
     *
     * @param configuration the configuration.
     */
    private void initKernel(Map<String, String> configuration) {
        List<FrameworkMethod> beforeKernelMethods = getTestClass().getAnnotatedMethods(BeforeKernel.class);
        for (FrameworkMethod beforeKernelMethod : beforeKernelMethods) {
            try {
                beforeKernelMethod.invokeExplosively(null);
            } catch (Throwable throwable) {
                throw SeedException.wrap(throwable, ITErrorCode.EXCEPTION_OCCURRED_BEFORE_KERNEL);
            }
        }

        Exception eventualException = null;
        try {
            kernel = Seed.createKernel(null, provideKernelConfiguration(configuration), true);
        } catch (Exception e) {
            eventualException = e;
            kernel = null;
        }

        try {
            processException(eventualException, Expect.TestingStep.STARTUP);
        } catch (Exception e) {
            throw SeedException.wrap(e, ITErrorCode.FAILED_TO_INITIALIZE_KERNEL);
        }
    }

    private void stopKernel() {
        try {
            if (kernel != null && kernel.isStarted()) {
                Exception eventualException = null;

                try {
                    Seed.disposeKernel(kernel);
                } catch (Exception e) {
                    eventualException = e;
                }

                // Execute the AfterKernel methods before processing eventual exception
                List<FrameworkMethod> afterKernelMethods = getTestClass().getAnnotatedMethods(AfterKernel.class);
                for (FrameworkMethod afterKernelMethod : afterKernelMethods) {
                    try {
                        afterKernelMethod.invokeExplosively(null);
                    } catch (Throwable t) {
                        throw SeedException.wrap(t, ITErrorCode.EXCEPTION_OCCURRED_AFTER_KERNEL);
                    }
                }

                try {
                    processException(eventualException, Expect.TestingStep.SHUTDOWN);
                } catch (Exception e) {
                    throw SeedException.wrap(e, ITErrorCode.FAILED_TO_STOP_KERNEL);
                }
            }
        } finally {
            kernel = null;
        }
    }

    private void processException(Exception e, Expect.TestingStep testingStep) {
        Throwable unwrappedThrowable = e;

        // Unwrap known Guice exceptions to access the real one
        if (unwrappedThrowable != null && ProvisionException.class.isAssignableFrom(e.getClass()) && e.getCause() != null) {
            unwrappedThrowable = e.getCause();
        }

        if (expectedClass == null && unwrappedThrowable != null) {
            throw SeedException.wrap(unwrappedThrowable, ITErrorCode.UNEXPECTED_EXCEPTION_OCCURRED).put("occurredClass", unwrappedThrowable.getClass().getCanonicalName());
        }

        if (expectedClass != null && expectedTestingStep == testingStep) {
            if (unwrappedThrowable == null) {
                throw SeedException.createNew(ITErrorCode.EXPECTED_EXCEPTION_DID_NOT_OCCURRED).put("expectedClass", expectedClass.getCanonicalName());
            } else if (!expectedClass.isAssignableFrom(unwrappedThrowable.getClass())) {
                throw SeedException.createNew(ITErrorCode.ANOTHER_EXCEPTION_THAN_EXPECTED_OCCURRED).put("expectedClass", expectedClass.getCanonicalName()).put("occurredClass", unwrappedThrowable.getClass().getCanonicalName());
            }
        }
    }

    private KernelConfiguration provideKernelConfiguration(Map<String, String> configuration) {
        KernelConfiguration kernelConfiguration = NuunCore.newKernelConfiguration();

        if (getTestClass().getJavaClass().getAnnotation(WithoutSpiPluginsLoader.class) != null) {
            kernelConfiguration.withoutSpiPluginsLoader();
        }

        WithPlugins annotation = getTestClass().getJavaClass().getAnnotation(WithPlugins.class);
        if (annotation != null) {
            kernelConfiguration.plugins(annotation.value());
        }

        kernelConfiguration.param(ITPlugin.IT_CLASS_NAME, getTestClass().getJavaClass().getName());
        for (Map.Entry<String, String> defaultConfigurationEntry : configuration.entrySet()) {
            kernelConfiguration.param(ConfigurationPlugin.EXTERNAL_CONFIG_PREFIX + defaultConfigurationEntry.getKey(), defaultConfigurationEntry.getValue());
        }

        return kernelConfiguration;
    }

    private Map<String, String> gatherConfiguration(FrameworkMethod frameworkMethod) {
        Map<String, String> configuration = new HashMap<>();
        for (ITRunnerPlugin plugin : plugins) {
            Map<String, String> pluginConfiguration = plugin.provideConfiguration(getTestClass(), frameworkMethod);
            if (pluginConfiguration != null) {
                configuration.putAll(pluginConfiguration);
            }
        }
        return configuration;
    }

    private void notifyFailure(Throwable t, RunNotifier notifier) {
        notifier.fireTestFailure(new Failure(getDescription(), t));
    }
}
