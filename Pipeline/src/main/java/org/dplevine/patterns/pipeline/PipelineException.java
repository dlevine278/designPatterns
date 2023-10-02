package org.dplevine.patterns.pipeline;

public class PipelineException extends Exception {

    private Exception exception;

    public PipelineException(String description) {
        super(description);
    }

    public PipelineException(Exception exception) {
        this.exception = exception;
    }

    public Exception getException() {
        return exception;
    }
}
