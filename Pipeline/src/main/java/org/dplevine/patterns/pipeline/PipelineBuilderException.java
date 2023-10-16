package org.dplevine.patterns.pipeline;

/**
 * The PipelineBuilderException class is a custom exception class of the pipeline framework.
 * It a specific type of exception extending the PipelineException to represent exceptions that occur
 * during the construction of a pipeline.
 */
public class PipelineBuilderException extends PipelineException {

    public PipelineBuilderException(String description) {
        super(description);
    }

    public PipelineBuilderException(Exception exception) {
        super(exception);
    }
}
