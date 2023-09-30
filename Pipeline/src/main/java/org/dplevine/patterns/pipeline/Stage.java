package org.dplevine.patterns.pipeline;

public interface Stage {

    ExecutionContext doWork(ExecutionContext context) throws Exception;
}
