package org.dplevine.patterns.pipeline;

/**
 * This interface is part of a pipeline or workflow system. It defines a generic method that can be implemented by various stages
 * or steps in a pipeline. Each stage will take an input ExecutionContext, perform some work, and return an updated ExecutionContext.
 */
public interface Stage {

    ExecutionContext doWork(ExecutionContext context) throws Exception;
}
