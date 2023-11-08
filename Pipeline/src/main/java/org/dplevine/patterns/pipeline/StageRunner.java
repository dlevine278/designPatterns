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
        try {
            stageWrapper.init(context);
            context.createEvent(stageWrapper, ExecutionContext.EventType.CALLING_STAGE, stageWrapper.getStage().getClass().getCanonicalName() + ".doWork()");
            stageWrapper.doWork(context);
            context.createEvent(stageWrapper, ExecutionContext.EventType.CALLED_STAGE, stageWrapper.getStage().getClass().getCanonicalName() + ".doWork()");
        } catch (Exception e) {
            context.createEvent(stageWrapper, ExecutionContext.EventType.EXCEPTION, stageWrapper.getStage().getClass().getCanonicalName() + ": " + e.getLocalizedMessage());
            if (context.getFastFail()) {
                throw new PipelineExecutionException(e);
            }
        } finally {
            stageWrapper.close(context);
        }
        return context;
    }

    ExecutionContext getContext() {
        return context;
    }
}
