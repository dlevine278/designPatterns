package org.dplevine.patterns.pipeline;

/**
 * The StageWrapperCallback class defines a common structure for callback classes that can be registered with StageWrapper instances
 * to execute custom logic at specific stages of execution.  This is not exposed outside of the framework.
 */
abstract class StageWrapperCallback {
    private StageWrapper stageWrapper;

    StageWrapperCallback(StageWrapper stageWrapper) {
        this.stageWrapper = stageWrapper;
    }

    abstract void doWork(StageWrapper stageWrapper, ExecutionContext context);
}
