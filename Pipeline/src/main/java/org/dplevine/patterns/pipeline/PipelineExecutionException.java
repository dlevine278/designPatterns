package org.dplevine.patterns.pipeline;

/**
 * The PipelineBuilderException class is a custom exception class of the pipeline framework.
 * It a specific type of exception extending the PipelineException to represent exceptions that occur
 * during the execution of a pipeline.
 */
public class PipelineExecutionException extends PipelineException {

    public PipelineExecutionException(String description) {
        super(description);
    }

    public PipelineExecutionException(Exception exception) {
        super(exception);
    }
}
