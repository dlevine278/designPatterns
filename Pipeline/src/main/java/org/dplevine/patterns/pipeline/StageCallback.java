package org.dplevine.patterns.pipeline;

public interface StageCallback {

    public void doCallback(String stageId, Stage stage, ExecutionContext context);
}
