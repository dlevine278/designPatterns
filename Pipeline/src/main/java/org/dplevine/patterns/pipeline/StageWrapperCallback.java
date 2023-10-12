package org.dplevine.patterns.pipeline;

abstract class StageWrapperCallback {
    private StageWrapper stageWrapper;

    StageWrapperCallback(StageWrapper stageWrapper) {
        this.stageWrapper = stageWrapper;
    }

    abstract void doWork(StageWrapper stageWrapper, ExecutionContext context);
}
