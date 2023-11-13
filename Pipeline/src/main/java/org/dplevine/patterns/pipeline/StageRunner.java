package org.dplevine.patterns.pipeline;

public class  StageRunner {
    ExecutionContext context;

    /**
     * The StageRunner class is responsible for initializing, running, and closing a StageWrapper.
     * It manages the execution of a stage and handles exceptions.
     *
     * The StageRunner class encapsulates the logic for initializing, running, and closing a StageWrapper.
     * It also logs events during the process.
     * @param context
     */
    StageRunner(ExecutionContext context) {
        this.context = context;
    }

    ExecutionContext run(StageWrapper stageWrapper) throws Exception {
        StageCallback.StageEvent event = StageCallback.StageEvent.PostStageCallbackSuccess;;
        try {
            // call the wrapper init first
            stageWrapper.init(context);
            // invoke any application registered pre-stage callbacks
            stageWrapper.doPreStageCallbacks(context, StageCallback.StageEvent.PreStageCallback);
            context.createEvent(stageWrapper, ExecutionContext.EventType.CALLING_STAGE, stageWrapper.getStage().getClass().getCanonicalName() + ".doWork()");
            // invoke the stage (indirectly via the wrapper)
            stageWrapper.doWork(context);
            context.createEvent(stageWrapper, ExecutionContext.EventType.CALLED_STAGE, stageWrapper.getStage().getClass().getCanonicalName() + ".doWork()");
            event = StageCallback.StageEvent.PostStageCallbackSuccess;
        } catch (Exception e) {
            context.createEvent(stageWrapper, ExecutionContext.EventType.EXCEPTION, stageWrapper.getStage().getClass().getCanonicalName() + ": " + e.getLocalizedMessage());
            event = StageCallback.StageEvent.PostStageCallbackError;
            if (context.getFastFail()) {
                context.setFailNow(true);
                throw new PipelineExecutionException(e);
            }
        } finally {
            // invoke any application registered post-stage callbacks
            stageWrapper.doPostStageCallbacks(context, event);
            stageWrapper.close(context);
        }
        return context;
    }

    ExecutionContext getContext() {
        return context;
    }
}
