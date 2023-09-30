package org.dplevine.patterns.pipeline;

public class  StageRunner {
    ExecutionContext context;

    StageRunner(ExecutionContext context) {
        this.context = context;
    }

    StageRunner() {
        this.context = new ExecutionContext();
    }

    ExecutionContext run(StageWrapper stage) throws Exception {
        return stage.doWork(context);
    }
}
