package org.dplevine.patterns.pipeline;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

final class Fork extends StageWrapper { // will change visibility once the builder is complete
    private final int THREADPOOL_SIZE = 10;  // size of threadpool = number of concurrent pipelines that can run at any given time

    private ExecutorService executorService;
    private static Logger logger = LoggerFactory.getLogger(Fork.class);
    private final List<Pipeline> forkPipelines = new Vector<>();

    //ctors
    Fork(String id) {
        super(id);
    }

    Fork(String id, List<Pipeline> forkPipelines) {
        super(id);
        this.forkPipelines.addAll(forkPipelines);
    }

    Fork addPipeline(Pipeline forkPipeline) {
        forkPipelines.add(forkPipeline);
        return this;
    }

    // or we can add lists stages
    Fork addPipelines(List<Pipeline> forkPipelines) {
        this.forkPipelines.addAll(forkPipelines);
        return this;
    }

    @Override
    final void init(ExecutionContext context) {
        executorService = Executors.newFixedThreadPool(THREADPOOL_SIZE); // this is what manages the concurrency
    }

    @Override
    final void close(ExecutionContext context) {
        executorService.shutdown();  // clean up all the threads (after execution has completed)
    }

    // because this class is derived from PipelineStage, it implements the doWork interface (abstract method)
    // this method is what kicks off and manages the concurrent execution of all of the sub-stages
    // it does not return until either all sub-stages have completed OR an exception is thrown
    @Override
    public ExecutionContext doWork(ExecutionContext context) throws Exception {

        for(Pipeline forkPipeline : forkPipelines) {
            forkPipeline.setContext(context);
        }

        try {
            executorService.invokeAll(forkPipelines);  // execute all the pipelines being forked (order of execution is non-deterministic)
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Fork execution failed: " + e.getLocalizedMessage());
            throw new ExecutionException(e);
        }
        return context;
    }

    @Override
    String buildGraph(String root, Graph<String, DefaultEdge> pipelineGraph) {
        String startId = this.getId() + " - [Fork Start]";
        String endId = this.getId() + " - [Fork End]";
        pipelineGraph.addVertex(startId);
        pipelineGraph.addVertex(endId);
        pipelineGraph.addEdge(root, startId);

        for (Pipeline forkPipeline : forkPipelines) {
            pipelineGraph.addEdge(forkPipeline.buildGraph(startId, pipelineGraph),endId);
        }

        return endId;
    }
}
