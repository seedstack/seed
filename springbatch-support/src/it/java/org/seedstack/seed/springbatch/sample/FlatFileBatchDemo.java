/**
 * Copyright (c) 2013-2015 by The SeedStack authors. All rights reserved.
 *
 * This file is part of SeedStack, An enterprise-oriented full development stack.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.springbatch.sample;

import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.HashMap;
import java.util.Map;

public class FlatFileBatchDemo {

    public static void main(String[] argv) throws Exception {
        String jobFile = argv[0];

        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext(new String[]{"simple-job-launcher-context.xml", jobFile});

        Job job = applicationContext.getBean(Job.class);

        Map<String, JobParameter> jobParametersMap = new HashMap<String, JobParameter>();

        jobParametersMap.put("file", new JobParameter(argv[1]));

        JobParameters jobParameters = new JobParameters(jobParametersMap);

        JobLauncher jobLauncher = applicationContext.getBean(JobLauncher.class);

        JobExecution jobExecution = jobLauncher.run(job, jobParameters);

        printStatistics(jobExecution);
    }

    protected static void printStatistics(JobExecution jobExecution) {
        for (StepExecution stepExecution : jobExecution.getStepExecutions()) {
            System.out.println("-----------------------------------");
            System.out.println("STEP name: " + stepExecution.getStepName());
            System.out.println("-----------------------------------");
            System.out.println("CommitCount: " + stepExecution.getCommitCount());
            System.out.println("FilterCount: " + stepExecution.getFilterCount());
            System.out.println("ProcessSkipCount: " + stepExecution.getProcessSkipCount());
            System.out.println("ReadCount: " + stepExecution.getReadCount());
            System.out.println("ReadSkipCount: " + stepExecution.getReadSkipCount());
            System.out.println("RollbackCount: " + stepExecution.getRollbackCount());
            System.out.println("SkipCount: " + stepExecution.getSkipCount());
            System.out.println("WriteCount: " + stepExecution.getWriteCount());
            System.out.println("WriteSkipCount: " + stepExecution.getWriteSkipCount());
            if (stepExecution.getFailureExceptions().size() > 0) {
                System.out.println("exceptions:");
                System.out.println("-----------------------------------");
                for (Throwable t : stepExecution.getFailureExceptions()) {
                    System.out.println(t.getMessage());
                }
            }
            System.out.println("-----------------------------------");
        }
    }

}
