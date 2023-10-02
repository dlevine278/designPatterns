package org.dplevine.patterns.pipeline;

public class PipelineExecutionException extends PipelineException {

    public PipelineExecutionException(String description) {
        super(description);
    }

    public PipelineExecutionException(Exception exception) {
        super(exception);
    }
}
