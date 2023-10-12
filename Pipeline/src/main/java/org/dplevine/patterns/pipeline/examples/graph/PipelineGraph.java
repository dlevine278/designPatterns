package org.dplevine.patterns.pipeline.examples.graph;

import org.dplevine.patterns.pipeline.*;

import java.net.URL;
import java.util.concurrent.Future;

public class PipelineGraph {

    static final String PIPLINE_GRAPH = "/pipelineGraph.yaml";

    static final Long MAX_DELAY = 5L;
    static final String GRAPH_PATHNAME = "/tmp/pipelinegraph";
    static final Pipeline.ImageType GRAPH_TYPE = Pipeline.ImageType.GIF;

    static class TimerContext extends ExecutionContext {
        public static final String MAX_DELAY_SECONDS = "MaxDelay";
        public static final String PIPELINE = "Pipeline";

        public TimerContext() {
            super();
        }

        public Long getMaxDelay() {
            return (Long) getObject(MAX_DELAY_SECONDS);
        }

        public void setMaxDelay(Long maxDelay) {
            addObject(MAX_DELAY_SECONDS, maxDelay);
        }

        public void setPipeline(Pipeline pipeline) {
            addObject(PIPELINE, pipeline);
        }

        public Pipeline getPipeline() {
            return (Pipeline) getObject(PIPELINE);
        }
    }

    public static class TimerStage implements Stage, StageBuilder {
        public TimerStage () {}

        @Override
        public ExecutionContext doWork(ExecutionContext context) throws Exception {
            PipelineGraph.TimerContext timerContext = (PipelineGraph.TimerContext) context;
            long delay = Math.round(timerContext.getMaxDelay() * 1000 * Math.random());
            Thread.sleep(Math.round(delay));

            if (Math.round(Math.random()) == 1) {
                throw new Exception("simulated exception");
            }
            return context;
        }

        @Override
        public Stage buildStage() {
            return new PipelineGraph.TimerStage();
        }
    }

    public static void main(String args[]) throws Exception {
        PipelineBuilder builder = PipelineBuilder.createBuilder();

        URL url = PipelineGraph.class.getResource(PIPLINE_GRAPH);
        Pipeline pipeline = builder.buildFromPathName(url.getPath());

        TimerContext context = new TimerContext();
        context.setMaxDelay(MAX_DELAY);
        context.setPipeline(pipeline);

        pipeline.render(GRAPH_PATHNAME, GRAPH_TYPE);
        Thread.sleep(2000);

        Future<ExecutionContext> future = pipeline.runDetached(context);
        while (!future.isDone()) {
            pipeline.render(GRAPH_PATHNAME, GRAPH_TYPE);
            Thread.sleep(2000);
        }

        pipeline.render(GRAPH_PATHNAME, GRAPH_TYPE);

        System.out.println(context.getEventLog());
    }
}
