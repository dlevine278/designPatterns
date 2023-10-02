package org.dplevine.patterns.pipeline;

public class PipelineBuilderException extends PipelineException {

    public PipelineBuilderException(String description) {
        super(description);
    }

    public PipelineBuilderException(Exception exception) {
        super(exception);
    }
}
