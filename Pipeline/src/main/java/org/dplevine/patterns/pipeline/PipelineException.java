package org.dplevine.patterns.pipeline;

/**
 * The PipelineException class is a custom exception class used in the pipeline framework.
 */
public class PipelineException extends Exception {

    private Exception exception;

    public PipelineException(String description) {
        super(description);
    }

    public PipelineException(Exception exception) {
        super(exception.getMessage());
        this.exception = exception;
    }

    public Exception getException() {
        return exception;
    }
}
