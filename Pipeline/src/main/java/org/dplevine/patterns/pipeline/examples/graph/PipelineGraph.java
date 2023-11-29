package org.dplevine.patterns.pipeline.examples.graph;

import org.dplevine.patterns.pipeline.*;

import java.net.URL;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Future;


/**
 * The PipelineGraph class demonstrates the use of the pipeline framework to create a pipeline with stages
 * involving timers and random exception as a means of showcasing how to visualize the pipeline's execution and
 * capture its event log for monitoring and debugging.
 */
public class PipelineGraph {

    static final String PIPLINE_GRAPH = "/pipelineGraph.yaml";

    static final Long MAX_DELAY = 5L;
    static final String GRAPH_PATHNAME = "/tmp/pipelinegraph";
    static final Pipeline.ImageType GRAPH_TYPE = Pipeline.ImageType.GIF;

    public static class TimerContext extends ExecutionContext {
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

    @StageBuilderDefinition(id = "stage 1")
    @StageBuilderDefinition(id = "stage 2")
    @StageBuilderDefinition(id = "stage 3")
    @StageBuilderDefinition(id = "stage 4")
    @StageBuilderDefinition(id = "stage 5")
    @StageBuilderDefinition(id = "stage 6")
    @StageBuilderDefinition(id = "stage 7")
    @StageBuilderDefinition(id = "stage 8")
    @StageBuilderDefinition(id = "stage 9")
    @PipelineStepsDefinition(pipelineRootId = "Test", parallelId = "p1", steps = {"stage 1", "stage 2"})
    @PipelineStepsDefinition(pipelineRootId = "Test", parallelId = "p1", steps = {"stage 3", "stage 4"})
    @PipelineStepsDefinition(pipelineRootId = "Test", steps = {"stage 5", "p1", "stage 6"})
    @PipelineStepsDefinition(pipelineRootId = "Test2", steps = {"stage 1", "stage 2"})
    @PipelineStepsDefinition(pipelineRootId = "Test3", steps = {"stage 3", "stage 4"})
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
        List<String> packages = new Vector<>();
        packages.add(PipelineGraph.class.getPackageName());
        PipelineBuilder builder = PipelineBuilder.createBuilder(packages);

        URL url = PipelineGraph.class.getResource(PIPLINE_GRAPH);
        //Pipeline pipeline = builder.buildFromPathName(url.getPath());
        Pipeline pipeline = builder.buildFromAnnotations("Test");

        TimerContext context = new TimerContext();
        context.setMaxDelay(MAX_DELAY);
        context.setPipeline(pipeline);

        pipeline.render(GRAPH_PATHNAME, GRAPH_TYPE);
        Thread.sleep(2000);

        Future<ExecutionContext> future = pipeline.runDetached(context, false);
        while (!future.isDone()) {
            pipeline.render(GRAPH_PATHNAME, GRAPH_TYPE);
            Thread.sleep(2000);
        }

        pipeline.render(GRAPH_PATHNAME, GRAPH_TYPE);

        System.out.println(context.getEventLog());
    }
}