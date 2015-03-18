/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.springbatch.internal;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import org.seedstack.seed.cli.spi.CliOption;
import org.seedstack.seed.cli.spi.CommandLineHandler;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.job.AbstractJob;
import org.springframework.batch.core.launch.JobLauncher;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

class SpringBatchCommandLineHandler implements CommandLineHandler {

    private static final String DEFAULT_JOB_LAUNCHER_NAME = "jobLauncher";

    private static final String DEFAULT_JOB_NAME = "job";

    private static final Logger LOGGER = LoggerFactory.getLogger(SpringBatchCommandLineHandler.class);

    @CliOption(opt = "l", longOpt = "jobLauncher", optionalArg = true, description = "Spring Job Launcher Name")
    String optionJobLauncherName;

    @CliOption(opt = "j", longOpt = "job", optionalArg = true, description = "Spring Job Name")
    String optionJobName;

    @CliOption(opt = "P", longOpt = "jobParameter", args = true, valueSeparator = '=', description = "Spring Job parameter")
    String[] optionJobParameters;

    @Inject
    Injector injector;

    @Override
    public String name() {
        return "spring-batch-cli-handler";
    }

    @Override
    public Integer call() throws JobExecutionException {
        Integer batchExitStatus = 0;

        JobLauncher jobLauncher = getJobLauncher();

        Job job = getJob();

        JobParameters jobParameters = getJobParameters();

        JobExecution jobExecution = jobLauncher.run(job, jobParameters);

        BatchStatus batchStatus = jobExecution.getStatus();

        if (!batchStatus.equals(BatchStatus.COMPLETED)) {
            batchExitStatus = 1;
        }

        LOGGER.info("Exit with status : " + batchStatus);
        return batchExitStatus;
    }

    private JobParameters getJobParameters() {
        JobParameters jobParameters;
        Map<String, JobParameter> mapJobParameter = optionParameters(optionJobParameters);
        if (mapJobParameter != null && !mapJobParameter.isEmpty()) {
            jobParameters = new JobParameters(mapJobParameter);
        } else {
            jobParameters = new JobParameters();
        }
        return jobParameters;
    }

    private Map<String, JobParameter> optionParameters(String[] optJobParameters) {
        Map<String, JobParameter> mapJobParameter = null;
        if (optJobParameters != null && optJobParameters.length > 0 && (optJobParameters.length % 2) == 0) {
            mapJobParameter = new HashMap<String, JobParameter>();
            for (int i = 0; i < optJobParameters.length; i = i + 2) {
                mapJobParameter.put(optJobParameters[i], new JobParameter(optJobParameters[i + 1]));
            }
        }
        return mapJobParameter;
    }

    private Job getJob() {

        String jobName = option(optionJobName, DEFAULT_JOB_NAME);

        return injector.getInstance(Key.get(AbstractJob.class, Names.named(jobName)));
    }

    private JobLauncher getJobLauncher() {

        String jln = option(optionJobLauncherName, DEFAULT_JOB_LAUNCHER_NAME);
        return injector.getInstance(Key.get(JobLauncher.class, Names.named(jln)));
    }

    String option(String option, String defaultName) {
        String ret = defaultName;

        if (!StringUtils.isBlank(option)) {
            ret = option;
        }

        return ret;

    }

}
