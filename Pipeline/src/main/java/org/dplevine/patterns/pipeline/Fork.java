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
    private final String FORK_START_TAG  = " - <Fork>";
    private final String FORK_END_TAG = " - </Fork>";

    private ExecutorService executorService;
    private static final Logger logger = LoggerFactory.getLogger(Fork.class);
    private final List<Pipeline> forkPipelines = new Vector<>();

    //ctors
    Fork(String id) {
        super(id);
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
    final ExecutionContext init(ExecutionContext context) {
        setStage(this);
        executorService = Executors.newFixedThreadPool(THREADPOOL_SIZE); // this is what manages the concurrency
        return context;
    }

    @Override
    final ExecutionContext close(ExecutionContext context) {
        executorService.shutdown();  // clean up all the threads (after execution has completed)
        return context;
    }

    // because this class is derived from PipelineStage, it implements the doWork interface (abstract method)
    // this method is what kicks off and manages the concurrent execution of all of the sub-stages
    // it does not return until either all sub-stages have completed OR an exception is thrown
    @Override
    public ExecutionContext doWork(ExecutionContext context) throws Exception {

        forkPipelines.forEach(forkPipeline -> forkPipeline.setContext(context));

        List<String> forkPipelineIds = new Vector<>();  // for the event log
        forkPipelines.forEach( forkPipeline -> forkPipelineIds.add(forkPipeline.getId()));
        try {
            context.createEvent(this, ExecutionContext.EventType.CALLING_STAGE, "Concurrently invoking : " + forkPipelineIds);
            executorService.invokeAll(forkPipelines);  // execute all the pipelines being forked (order of execution is non-deterministic)
            context.createEvent(this, ExecutionContext.EventType.CALLED_STAGE, "Concurrently invoked pipelines: " + forkPipelineIds);
        } catch (Exception e) {
            logger.error("Fork execution failed: " + e.getLocalizedMessage());
            context.createEvent(this, ExecutionContext.EventType.EXCEPTION, "Exception in one of the pipelines belonging to fork " + getId());
            throw new PipelineExecutionException(e);
        }
        return context;
    }

    @Override
    String buildGraph(String root, Graph<String, DefaultEdge> pipelineGraph) {
        String startId = this.getId() + FORK_START_TAG;
        String endId = this.getId() + FORK_END_TAG;
        pipelineGraph.addVertex(startId);
        pipelineGraph.addVertex(endId);

        pipelineGraph.addEdge(root, startId);
        forkPipelines.forEach(forkPipeline -> pipelineGraph.addEdge(forkPipeline.buildGraph(startId, pipelineGraph),endId));
        return endId;
    }
}
