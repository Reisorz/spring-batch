package com.mls.spring_batch.decision;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FlowDecision implements JobExecutionDecider {
    @Override
    public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {

        String stepName = stepExecution.getStepName();

        List<Throwable> allFailureExceptions = jobExecution.getAllFailureExceptions();

        if (allFailureExceptions.size() > 10) return FlowExecutionStatus.FAILED;

        System.out.println("Step " + stepName + " COMPLETED");

        return FlowExecutionStatus.COMPLETED;
    }
}