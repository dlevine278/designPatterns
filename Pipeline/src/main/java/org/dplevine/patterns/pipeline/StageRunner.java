package org.dplevine.patterns.pipeline;

public class  StageRunner {
    ExecutionContext context;

    StageRunner(ExecutionContext context) {
        this.context = context;
    }

    ExecutionContext run(StageWrapper stageWrapper) throws Exception {
        try {
            stageWrapper.init(context);
            context.createEvent(stageWrapper, ExecutionContext.EventType.CALLING_STAGE, stageWrapper.getStage().getClass().getCanonicalName() + ".doWork()");
            stageWrapper.doWork(context);
            context.createEvent(stageWrapper, ExecutionContext.EventType.CALLED_STAGE, stageWrapper.getStage().getClass().getCanonicalName() + ".doWork()");
            stageWrapper.close(context);
        } catch (Exception e) {
            context.createEvent(stageWrapper, ExecutionContext.EventType.EXCEPTION, stageWrapper.getStage().getClass().getCanonicalName() + e.getLocalizedMessage());
            stageWrapper.close(context);
            throw new PipelineExecutionException(e);
        }
        return context;
    }

    ExecutionContext getContext() {
        return context;
    }
}
