package org.dplevine.patterns.pipeline;

/**
 * The StageCallback interface defines a contract for callback objects that can be used to execute custom logic at specific points
 * during the execution of a stage in a pipeline (i.e., before and after the execution of a stage)
 */
public interface StageCallback {

    enum StageEvent {
        PreStageCallback,
        PostStageCallbackSuccess,
        PostStageCallbackError,
    }

    void onEvent(String stageId, Stage stage, StageEvent event, ExecutionContext context);
}
